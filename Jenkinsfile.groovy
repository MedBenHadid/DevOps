pipeline {
    agent {
        docker {
            image 'maven:3.8.1-adoptopenjdk-11'
            args '-v /root/.m2:/root/.m2'
        }
    }
    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker')
        IMAGE = readMavenPom().getArtifactId()
        VERSION = readMavenPom().getVersion()
    }
    stages {
        stage('Test') {
            steps{
                withMaven {
                    sh "mvn clean test"
                }
            }
        }
        stage('SonarQube Analysis') {
            steps{
                withSonarQubeEnv('Default SonarQube') {
                    sh "mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar -DskipTests"
                }
            }
        }
        stage('Local Integration Tests') {
            steps{
                withMaven{
                    sh "mvn -B org.jacoco:jacoco-maven-plugin:prepare-agent-integration failsafe:integration-test failsafe:verify -DskipTests"
                }
            }
        }
        stage('Build'){
            steps{
                sh "mvn clean install -DskipTests"
            }
        }
        stage('Build docker image') {
            steps {
                sh "docker build -t med21/${IMAGE}:${VERSION} ."
            }
        }
        stage('Login to docker') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
            }
        }
        stage('Push to docker') {
            steps {
                sh "docker push med21/${IMAGE}:${VERSION}"
            }
        }
        stage('Deploy to Nexus') {
            steps{
                withMaven {
                    sh "mvn clean package --settings settings.xml deploy:deploy-file -DgroupId=tn.esprit.spring -DartifactId=${IMAGE} -Dversion=${VERSION} -DgeneratePom=true -Dpackaging=jar -DrepositoryId=nexus -Durl=https://c973-196-234-185-116.eu.ngrok.io/repository/maven-releases/ -Dfile=target/${IMAGE}-${VERSION}.jar -DskipTests"
                    archiveArtifacts artifacts: '**/timesheet-*.jar', onlyIfSuccessful: false
               }
            }
        }
    }
    post {
        always {
            sh 'docker logout'
            deleteDir()
        }
        failure {
            mail to: 'mohamed.benhadid@esprit.tn',
                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}'s test"
        }
        success {
            mail to: 'mohamed.benhadid@esprit.tn',
                    subject: "Successfull Pipeline: ${currentBuild.fullDisplayName}",
                    body: "${env.BUILD_URL}'s test and build all passed"
        }

    }
}
