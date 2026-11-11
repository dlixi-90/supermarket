FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM tomcat:9.0-jdk17-temurin-jammy
RUN sed -i -e 's/<Server port="8005"/<Server port="-1"/' \
    -e 's/port="8080"/address="0.0.0.0" port="${http.port}"/' "$CATALINA_HOME/conf/server.xml" \
    && rm -rf "$CATALINA_HOME/webapps/"*
COPY --from=build /app/target/msm.war "$CATALINA_HOME/webapps/ROOT.war"
COPY docker/entrypoint.sh /usr/local/bin/msm-entrypoint.sh
EXPOSE 10000
CMD ["sh", "/usr/local/bin/msm-entrypoint.sh"]
