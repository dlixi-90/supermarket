FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM tomcat:9.0-jdk17-temurin-jammy
RUN sed -i 's/port="8080"/port="10000"/' "$CATALINA_HOME/conf/server.xml" \
    && rm -rf "$CATALINA_HOME/webapps/"*
COPY --from=build /app/target/msm.war "$CATALINA_HOME/webapps/ROOT.war"
EXPOSE 10000
CMD ["catalina.sh", "run"]
