package it.unipi.githeritage.Populate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.ParserConfiguration;

import it.unipi.githeritage.DAO.MongoDB.ProjectMongoDAO;
import it.unipi.githeritage.DAO.Neo4j.Neo4jDAO;
import lombok.RequiredArgsConstructor;

@Profile("populateproject")
@Component
@RequiredArgsConstructor
public class ProjectsMongo2Neo4j implements CommandLineRunner {

    private final Neo4jDAO neo4jDAO;
    private final ProjectMongoDAO projectMongoDAO;
    private static final int BATCH_SIZE = 100; // Process projects in batches of 1000

    @Override
    public void run(String... args) throws Exception {
        // Configure JavaParser with SymbolResolver
        configureJavaParser();
        
        // Create index on project id for better performance
        neo4jDAO.createProjectIdIndex();
        System.out.println("Created constraint on project id attribute in Neo4j");
        
        // Read JSON file and parse it
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File filesJsonFile = new File("scraped_data/neo4j_files.json");
            JsonNode filesRootNode = objectMapper.readTree(filesJsonFile);
            
            int totalFiles = filesRootNode.size();
            System.out.println("Total files to process: " + totalFiles);
            
            // Process Java files in batches
            int processedFiles = 0;
            
            Map<String, Object> projectPackageName = new HashMap<>();
            Map<String, Object> projectDependencies = new HashMap<>();

            for (JsonNode fileNode : filesRootNode) {
                System.out.println("First pass [" + (++processedFiles) + "/" + totalFiles + "] Processing file: " + fileNode.get("path").asText());
                if (fileNode.get("path").asText().equals("pom.xml")) {
                    String projectId = fileNode.get("projectId").asText();
                    String owner = fileNode.get("owner").asText();
                    String projectName = fileNode.get("projectName").asText();
                    String repoFullname = owner + "/" + projectName;
                    String content = fileNode.get("content").asText();
                    
                    // Parse the pom.xml content to extract package name and dependencies
                    try {
                        String packageName = extractPackageFromPomXml(content);
                        if (packageName != null && !packageName.isEmpty()) {
                            projectPackageName.put(projectId, packageName);
                        }
                        
                        List<String> dependencies = extractDependenciesFromPomXml(content);
                        if (!dependencies.isEmpty()) {
                            projectDependencies.put(projectId, dependencies);
                        }
                    } catch (Exception e) {
                        // System.out.println("Failed to parse pom.xml for project " + projectId + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("Extracted package names and dependencies from pom.xml files");
            System.out.println("Total projects with package names: " + projectPackageName.size());
            System.out.println("Total projects with dependencies: " + projectDependencies.size());



            File jsonFile = new File("scraped_data/mongo_projects.json");
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            
            int totalProjects = rootNode.size();
            System.out.println("Total projects to process: " + totalProjects);
            
            // Extract all project data into a list of maps
            List<Map<String, Object>> allProjects = new ArrayList<>(totalProjects);
            for (JsonNode projectNode : rootNode) {
                Map<String, Object> project = new HashMap<>();
                project.put("id", projectNode.get("_id").asText());
                project.put("owner", projectNode.get("owner").asText());
                project.put("projectName", projectNode.get("name").asText());
                
                // Add administrators list to the project data
                if (projectNode.has("administrators") && projectNode.get("administrators").isArray()) {
                    List<String> administrators = new ArrayList<>();
                    for (JsonNode admin : projectNode.get("administrators")) {
                        administrators.add(admin.asText());
                    }
                    project.put("administrators", administrators);
                } else {
                    // If no administrators field, use owner as the default admin
                    project.put("administrators", List.of(projectNode.get("owner").asText()));
                }
                // Extract package name from pom.xml if available
                String projectId = project.get("id").toString();
                System.out.println("Processing project: " + projectId);
                String packageName = (String) projectPackageName.get(projectId);
                if (packageName != null && !packageName.isEmpty()) {
                    project.put("packageName", packageName);
                }
                
                allProjects.add(project);
            }
            
            // Process projects in batches
            for (int i = 0; i < allProjects.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, allProjects.size());
                List<Map<String, Object>> batch = allProjects.subList(i, endIndex);
                
                // Merge all projects in this batch
                neo4jDAO.mergeProjects(batch);
                
                System.out.println("[Project Progress: Batch " + (i/BATCH_SIZE + 1) + 
                                  "] Processed projects " + (i+1) + "-" + endIndex + 
                                  " of " + totalProjects);
            }

            // Create dependency relationships from pom.xml dependencies
            System.out.println("Creating Maven dependency relationships...");
            int processedDependencyProjects = 0;
            int totalDependencyProjects = projectDependencies.size();
            
            for (Map.Entry<String, Object> entry : projectDependencies.entrySet()) {
                String projectId = entry.getKey();
                @SuppressWarnings("unchecked")
                List<String> dependencies = (List<String>) entry.getValue();
                
                processedDependencyProjects++;
                System.out.println("Creating dependencies for project " + processedDependencyProjects + 
                                  "/" + totalDependencyProjects + " (" + dependencies.size() + " dependencies)");
                
                for (String dependency : dependencies) {
                    neo4jDAO.relateProjectDependencyByPackageName(projectId, dependency);
                }
            }

            // Process file data to add methods and dependencies
            //System.out.println("Processing file data to add methods and dependencies...");
            //File filesJsonFile = new File("scraped_data/neo4j_files.json");
            //JsonNode filesRootNode = objectMapper.readTree(filesJsonFile);
            
            //int totalFiles = filesRootNode.size();
            System.out.println("Total files to process: " + totalFiles);
            
            // Process Java files in batches
            processedFiles = 0;
            List<Map<String, Object>> methodCalls = new ArrayList<>(); // Store method calls for second pass
            
            for (JsonNode fileNode : filesRootNode) {
                processedFiles++;
                //if (processedFiles % 100 == 0) {
                    System.out.println("Processed " + processedFiles + " of " + totalFiles + " files");
                //}
                
                // Check if it's a Java file
                String path = fileNode.get("path").asText();
                // System.out.println("Processing file: " + path);
                if (!path.endsWith(".java")) {
                    continue;
                }
                
                try {
                    String projectId = fileNode.get("projectId").asText();
                    String owner = fileNode.get("owner").asText();
                    String content = fileNode.get("content").asText();
                    
                    // Parse the Java file content
                    CompilationUnit cu = StaticJavaParser.parse(content);
                    
                    // Extract all methods from the file
                    List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);

                    // System.out.println("Found " + methods.size() + " methods in file: " + path);
                    
                    // First pass: Process each method and create method nodes
                    for (MethodDeclaration md : methods) {
                        try {
                            ResolvedMethodDeclaration rmd = md.resolve();
                            String fqn = rmd.getQualifiedSignature();
                            String simpleName = rmd.getName();

                            // Analyze method complexity using static analysis
                            MethodMetrics metrics = analyzeMethodComplexity(md);

                            // System.out.println("Processing method: " + fqn + " in project: " + projectName);
                            
                            // Create method node in Neo4j with complexity metrics
                            neo4jDAO.mergeMethodWithMetrics(owner, fqn, simpleName, 
                                                          metrics.assignmentCount, 
                                                          metrics.arithmeticCount, 
                                                          metrics.loopCount);
                            
                            // Create relationship between project and method
                            neo4jDAO.relateProjectToMethod(projectId, owner, fqn);
                            
                            // Collect method calls for second pass
                            List<MethodCallExpr> methodCallExprs = md.findAll(MethodCallExpr.class);
                            for (MethodCallExpr call : methodCallExprs) {
                                try {
                                    String calleeFqn = call.resolve().getQualifiedSignature();
                                    // System.out.println("Found method call: " + calleeFqn + " in method: " + fqn);
                                    
                                    // Store for second pass
                                    Map<String, Object> callData = new HashMap<>();
                                    callData.put("owner", owner);
                                    callData.put("callerFqn", fqn);
                                    callData.put("calleeFqn", calleeFqn);
                                    methodCalls.add(callData);
                                } catch (Exception e) {
                                    // Skip if method call resolution fails
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            // Skip if method resolution fails
                            continue;
                        }
                    }
                    
                    // Extract package dependencies
                    // cu.getPackageDeclaration().ifPresent(pd -> {
                    //     String fullPackageName = pd.getNameAsString();
                    //     // Use full package name as dependency identifier
                    //     if (!fullPackageName.isEmpty()) {
                    //         neo4jDAO.relateProjectDependencyByPackageName(projectId, fullPackageName);
                    //     }
                    // });
                    
                } catch (Exception e) {
                    // System.out.println("Failed to parse file: " + path + " - " + e.getMessage());
                    continue;
                }
            }
            
            // Second pass: Create method call relationships
            System.out.println("Creating method call relationships...");
            for (Map<String, Object> callData : methodCalls) {
                String owner = (String) callData.get("owner");
                String callerFqn = (String) callData.get("callerFqn");
                String calleeFqn = (String) callData.get("calleeFqn");
                
                neo4jDAO.relateMethodsCall(owner, callerFqn, calleeFqn);
            }
            
            System.out.println("Completed processing all files");
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        
    }
    
    private void configureJavaParser() {
        // Set up type solvers
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        
        // Configure JavaParser with symbol resolver
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(configuration);
        
        System.out.println("Configured JavaParser with SymbolResolver");
    }
    
    /**
     * Parse pom.xml content and extract groupId.artifactId as package name
     */
    private String extractPackageFromPomXml(String pomContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Parse the XML content
            org.w3c.dom.Document doc = builder.parse(new java.io.ByteArrayInputStream(pomContent.getBytes()));
            doc.getDocumentElement().normalize();
            
            String groupId = null;
            String artifactId = null;
            
            // Get groupId
            NodeList groupIdNodes = doc.getElementsByTagName("groupId");
            if (groupIdNodes.getLength() > 0) {
                groupId = groupIdNodes.item(0).getTextContent().trim();
            }
            
            // Get artifactId
            NodeList artifactIdNodes = doc.getElementsByTagName("artifactId");
            if (artifactIdNodes.getLength() > 0) {
                artifactId = artifactIdNodes.item(0).getTextContent().trim();
            }
            
            // Return combined package name
            if (groupId != null && artifactId != null && !groupId.isEmpty() && !artifactId.isEmpty()) {
                return groupId + "." + artifactId;
            }
            
            return null;
            
        } catch (Exception e) {
            // If parsing fails, return null
            // System.out.println("Failed to parse pom.xml: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse pom.xml content and extract Maven dependencies
     */
    private List<String> extractDependenciesFromPomXml(String pomContent) {
        List<String> dependencies = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Parse the XML content
            org.w3c.dom.Document doc = builder.parse(new java.io.ByteArrayInputStream(pomContent.getBytes()));
            doc.getDocumentElement().normalize();
            
            // Get all dependency elements
            NodeList dependencyNodes = doc.getElementsByTagName("dependency");
            
            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Element dependencyElement = (Element) dependencyNodes.item(i);
                
                String groupId = null;
                String artifactId = null;
                
                // Get groupId from this dependency
                NodeList groupIdNodes = dependencyElement.getElementsByTagName("groupId");
                if (groupIdNodes.getLength() > 0) {
                    groupId = groupIdNodes.item(0).getTextContent().trim();
                }
                
                // Get artifactId from this dependency
                NodeList artifactIdNodes = dependencyElement.getElementsByTagName("artifactId");
                if (artifactIdNodes.getLength() > 0) {
                    artifactId = artifactIdNodes.item(0).getTextContent().trim();
                }
                
                // Add to dependencies list if both are present
                if (groupId != null && artifactId != null && !groupId.isEmpty() && !artifactId.isEmpty()) {
                    dependencies.add(groupId + "." + artifactId);
                }
            }
            
        } catch (Exception e) {
            // If parsing fails, return empty list
            // System.out.println("Failed to parse dependencies from pom.xml: " + e.getMessage());
        }
        
        return dependencies;
    }
    
    /**
     * Analyze method complexity by counting assignments, arithmetic operations, and loops
     */
    private MethodMetrics analyzeMethodComplexity(MethodDeclaration method) {
        int assignmentCount = 0;
        int arithmeticCount = 0;
        int loopCount = 0;
        
        // Count assignments
        assignmentCount += method.findAll(com.github.javaparser.ast.expr.AssignExpr.class).size();
        assignmentCount += method.findAll(com.github.javaparser.ast.expr.VariableDeclarationExpr.class).size();
        
        // Count arithmetic operations
        arithmeticCount += method.findAll(com.github.javaparser.ast.expr.BinaryExpr.class)
            .stream()
            .mapToInt(expr -> {
                switch (expr.getOperator()) {
                    case PLUS, MINUS, MULTIPLY, DIVIDE, REMAINDER -> {
                        return 1;
                    }
                    default -> {
                        return 0;
                    }
                }
            })
            .sum();
        arithmeticCount += method.findAll(com.github.javaparser.ast.expr.UnaryExpr.class)
            .stream()
            .mapToInt(expr -> {
                switch (expr.getOperator()) {
                    case PLUS, MINUS, PREFIX_INCREMENT, PREFIX_DECREMENT, 
                         POSTFIX_INCREMENT, POSTFIX_DECREMENT -> {
                        return 1;
                    }
                    default -> {
                        return 0;
                    }
                }
            })
            .sum();
        
        // Count loops
        loopCount += method.findAll(com.github.javaparser.ast.stmt.ForStmt.class).size();
        loopCount += method.findAll(com.github.javaparser.ast.stmt.ForEachStmt.class).size();
        loopCount += method.findAll(com.github.javaparser.ast.stmt.WhileStmt.class).size();
        loopCount += method.findAll(com.github.javaparser.ast.stmt.DoStmt.class).size();
        
        return new MethodMetrics(assignmentCount, arithmeticCount, loopCount);
    }
    
    /**
     * Simple data class to hold method complexity metrics
     */
    private static class MethodMetrics {
        final int assignmentCount;
        final int arithmeticCount;
        final int loopCount;
        
        MethodMetrics(int assignmentCount, int arithmeticCount, int loopCount) {
            this.assignmentCount = assignmentCount;
            this.arithmeticCount = arithmeticCount;
            this.loopCount = loopCount;
        }
    }
}

