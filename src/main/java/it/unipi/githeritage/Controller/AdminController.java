package it.unipi.githeritage.Controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {

    // DELETE /api/admin/user : delete arbitrary user
    // query parameters: username

    // DELETE /api/admin/project : delete project
    // query parameters: projectId

    // PUT /api/adimn/project : update project infos
    // query parameters: projectDTO

    // DELETE /api/admin/file : delete file
    // query parameters: projectId, path

    // DELETE /api/admin/file/{fileId} : delete file by Id

    // PUT /api/admin/file : update file
    // query parameters: fileDTO



}
