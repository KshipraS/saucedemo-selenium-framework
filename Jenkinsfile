pipeline {
    agent any

    tools {
        maven 'Maven3'   // Name must match a Maven installation configured in
                          // Jenkins > Manage Jenkins > Tools
        jdk 'JDK17'       // Same for a JDK17 installation
    }

    triggers {
        cron('H 2 * * *')   // runs automatically once daily, around 2 AM
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox', 'edge'], description: 'Browser to run against')
        choice(name: 'SUITE_FILE', choices: ['testng.xml', 'testng-crossbrowser.xml'], description: 'TestNG suite to execute')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run browser in headless mode')
        string(name: 'RETRY_COUNT', defaultValue: '1', description: 'Number of retries for failed tests')
        choice(name: 'ENV', choices: ['QA', 'STAGING', 'PROD'], description: 'Target environment')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean compile'
            }
        }

        stage('Run Tests') {
            steps {
                bat """
                    mvn -B test ^
                        -Dsuite.file=${params.SUITE_FILE} ^
                        -Dbrowser=${params.BROWSER} ^
                        -Dheadless=${params.HEADLESS} ^
                        -Dretry.count=${params.RETRY_COUNT} ^
                        -Denv=${params.ENV}
                """
            }
        }
    }

    post {
        always {
            // TestNG's own summary
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

            // Publish the ExtentReports HTML using the HTML Publisher plugin
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'test-output/extent-reports',
                reportFiles: '*.html',
                reportName: 'Extent Report'
            ])

            archiveArtifacts artifacts: 'test-output/**', allowEmptyArchive: true
        }

        success {
            emailext(
                subject: "PASSED: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <p>Build succeeded.</p>
                    <p>Environment: ${params.ENV} | Browser: ${params.BROWSER}</p>
                    <p>Report: <a href="${env.BUILD_URL}Extent_Report">View Extent Report</a></p>
                    <p>Console: <a href="${env.BUILD_URL}console">View Console Log</a></p>
                """,
                mimeType: 'text/html',
                to: 'qa-team@example.com',
                attachmentsPattern: 'test-output/extent-reports/*.html'
            )

            slackSend(
                channel: '#qa-automation',
                color: 'good',
                message: """
                    :white_check_mark: *PASSED*: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}
                    Environment: ${params.ENV} | Browser: ${params.BROWSER}
                    Report: ${env.BUILD_URL}Extent_Report
                """
            )
        }

        failure {
            emailext(
                subject: "FAILED: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <p>Build failed - please check the report and console log.</p>
                    <p>Environment: ${params.ENV} | Browser: ${params.BROWSER}</p>
                    <p>Report: <a href="${env.BUILD_URL}Extent_Report">View Extent Report</a></p>
                    <p>Console: <a href="${env.BUILD_URL}console">View Console Log</a></p>
                """,
                mimeType: 'text/html',
                to: 'qa-team@example.com',
                attachmentsPattern: 'test-output/extent-reports/*.html'
            )

            slackSend(
                channel: '#qa-automation',
                color: 'danger',
                message: """
                    :x: *FAILED*: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}
                    Environment: ${params.ENV} | Browser: ${params.BROWSER}
                    Report: ${env.BUILD_URL}Extent_Report
                    Console: ${env.BUILD_URL}console
                """
            )
        }
    }
}