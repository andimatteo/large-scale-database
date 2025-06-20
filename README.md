# LSMSDB project
Prima di runnare:
- cambiare credenziali default neo4j in `username=neo4j`,`password=password`
- non impostare credenziali particolari su mongodb


## popolare il database
<pre>
mongoimport --host 10.1.1.39 --port 27017 --db GitHeritage --collection Projects --file scraped_data/mongo_projects.json --jsonArray
mongoimport --host 10.1.1.39 --port 27017 --db GitHeritage --collection Files --file scraped_data/mongo_files.json --jsonArray
mongoimport --host 10.1.1.39 --port 27017 --db GitHeritage --collection Users --file scraped_data/mongo_users.json --jsonArray
mongoimport --host 10.1.1.39 --port 27017 --db GitHeritage --collection Commits --file scraped_data/mongo_commits.json --jsonArray

</pre>
Se si calcolano le ridondanze su un'altra macchina bisogna esportare e importare i valori corretti
<pre>
mongoexport --host 10.1.1.39 --port 27017 --db GitHeritage --collection Users --out scraped_data/mongo_users_with_redundancy.json --jsonArray

mongoimport --host 10.1.1.39 --port 27017 --db GitHeritage --collection Users --file scraped_data/mongo_users_with_redundancy.json --jsonArray
</pre>

Poi eseguire una volta il codice con 
`spring.profiles.active=populateuser` e poi con `spring.profiles.active=populateproject`. Ricordarsi di cambiarlo dopo l'ultima esecuzione (ad esempio a `nopopulate` prima dell'utilizzo normale)