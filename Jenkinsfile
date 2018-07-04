pipeline {
    agent { label 'docker' }
    stages {
        stage('Build') {
            steps {
                sh "docker build --tag ${GIT_COMMIT} ."
            }
        }
        stage('Publish') {
            when { branch 'master' }
            steps {
                sh "docker tag ${GIT_COMMIT} dtr.fintlabs.no/beta/betaling:latest"
                withDockerRegistry([credentialsId: 'dtr-fintlabs-no', url: 'https://dtr.fintlabs.no']) {
                    sh "docker push 'dtr.fintlabs.no/beta/betaling:latest'"
                }
                withDockerServer([credentialsId: "ucp-fintlabs-jenkins-bundle", uri: "tcp://ucp.fintlabs.no:443"]) {
                    sh "docker service update betaling-beta_betaling --image dtr.fintlabs.no/beta/betaling:latest --detach=false"
                }
            }
        }
        stage('Publish PR') {
            when { changeRequest() }
            steps {
                sh "docker tag ${GIT_COMMIT} dtr.fintlabs.no/beta/betaling:${BRANCH_NAME}"
                withDockerRegistry([credentialsId: 'dtr-fintlabs-no', url: 'https://dtr.fintlabs.no']) {
                    sh "docker push 'dtr.fintlabs.no/beta/betaling:${BRANCH_NAME}'"
                }
            }
        }
        stage('Publish Tag') {
            when { buildingTag() }
            steps {
                sh "docker tag ${GIT_COMMIT} dtr.fintlabs.no/beta/betaling:${TAG_NAME}"
                withDockerRegistry([credentialsId: 'dtr-fintlabs-no', url: 'https://dtr.fintlabs.no']) {
                    sh "docker push 'dtr.fintlabs.no/beta/betaling:${TAG_NAME}'"
                }
            }
        }
    }
}
