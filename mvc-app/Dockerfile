FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/mvc-app.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]