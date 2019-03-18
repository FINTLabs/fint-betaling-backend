pipeline {
    agent { label 'docker' }
    stages {
        stage('Build') {
            steps {
                withDockerRegistry([credentialsId: 'fintlabs.azurecr.io', url: 'https://fintlabs.azurecr.io']) {
                    sh "docker pull fintlabs.azurecr.io/fordring:latest"
                    sh "docker build --tag ${GIT_COMMIT} ."
                }
            }
        }
        stage('Publish') {
            when { branch 'master' }
            steps {
                withDockerRegistry([credentialsId: 'fintlabs.azurecr.io', url: 'https://fintlabs.azurecr.io']) {
                    sh "docker tag ${GIT_COMMIT} fintlabs.azurecr.io/betaling:build.${BUILD_NUMBER}"
                    sh "docker push fintlabs.azurecr.io/betaling:build.${BUILD_NUMBER}"
                }
            }
        }
        stage('Publish PR') {
            when { changeRequest() }
            steps {
                withDockerRegistry([credentialsId: 'fintlabs.azurecr.io', url: 'https://fintlabs.azurecr.io']) {
                    sh "docker tag ${GIT_COMMIT} fintlabs.azurecr.io/betaling:${BRANCH_NAME}.${BUILD_NUMBER}"
                    sh "docker push fintlabs.azurecr.io/betaling:${BRANCH_NAME}.${BUILD_NUMBER}"
                }
            }
        }
        stage('Publish Version') {
            when {
                tag pattern: "v\\d+\\.\\d+\\.\\d+(-\\w+-\\d+)?", comparator: "REGEXP"
            }
            steps {
                script {
                    VERSION = TAG_NAME[1..-1]
                }
                withDockerRegistry([credentialsId: 'fintlabs.azurecr.io', url: 'https://fintlabs.azurecr.io']) {
                    sh "docker tag ${GIT_COMMIT} fintlabs.azurecr.io/betaling:${VERSION}"
                    sh "docker push fintlabs.azurecr.io/betaling:${VERSION}"
                }
            }
        }
    }
}
