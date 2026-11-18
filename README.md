mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

 docker compose -f compose.keycloak.yml up -d

 Credenciais disponíveis no ambiente local:

Administrador: admin.rmg / admin123
Preenchedor: preenchedor.rmg / preenchedor123
Técnico: tecnico.rmg / tecnico123
Gestor: gestor.rmg / gestor123


http://localhost:3000/