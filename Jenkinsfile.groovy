pipeline {
    agent {
        docker {
            image 'maven:3.8.1-adoptopenjdk-11'
            args '-v /root/.m2:/root/.m2'
        }
    }
    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-login')
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
//                step([$class: 'JUnitResultArchiver', testResults: '**/target/failsafe-reports/TEST-*.xml'])
            }
        }
        stage('Build'){
            steps{
                sh "mvn clean install -DskipTests"
            }
        }
        stage('Build docker image') {
            steps {
                sh "docker build -t espritchihab/${IMAGE}:${VERSION} ."
            }
        }
        stage('Login to docker') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
            }
        }
        stage('Push to docker') {
            steps {
                sh "docker push espritchihab/${IMAGE}:${VERSION}"
            }
        }
        stage('Deploy to Nexus') {
            steps{
                withMaven {
                    sh "mvn clean package --settings settings.xml deploy:deploy-file -DgroupId=tn.esprit.spring -DartifactId=${IMAGE} -Dversion=${VERSION} -DgeneratePom=true -Dpackaging=jar -DrepositoryId=nexus -Durl=https://50be-197-1-239-58.eu.ngrok.io/repository/maven-releases/ -Dfile=target/${IMAGE}-${VERSION}.jar -DskipTests"
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
            mail to: 'chihab.hajji@esprit.tn',
                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}'s test"
            discordSend description: "${IMAGE}:${VERSION} Pipeline Build", footer: "Build successful", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "https://discord.com/api/webhooks/908327603428028447/WqNlqvRQhP2caIzVFKOoItlXZa7yJIXiQUVjrKIfGRfhU_W184n_zfm2uZGQZOeE1Oba"

        }
        success{
            discordSend description: "${IMAGE}:${VERSION} Pipeline Build", footer: "Build successful", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "https://discord.com/api/webhooks/908327603428028447/WqNlqvRQhP2caIzVFKOoItlXZa7yJIXiQUVjrKIfGRfhU_W184n_zfm2uZGQZOeE1Oba"
        }
    }
}
