def label = "${BUILD_TAG}"
label=label.replaceAll("%2F",'-')
def deployTime = Calendar.getInstance().getTime().format('YYYYMMdd-hhmmss',TimeZone.getTimeZone('CST'))
def version_tag = "1.99.3-dev"
def namespace = "shulan-dev"
def branchName = "${BRANCH_NAME}"
def credentialsId = "k8s-devops"
def gitAddress = "git@gitlab.chanjue.tech:sljj/shulan-jjr.git"
def buildCommand = ""
def img_url = ""
podTemplate(
    label: label,
    cloud: 'kubernetes',
    nodeSelector:'usage=devops-base',
    imagePullSecrets: [ 'default-secret' ],
    serviceAccount:"jenkins-kubectl",
    containers: [
        containerTemplate(
            name: 'maven',
            image: 'swr.cn-east-3.myhuaweicloud.com/chanjue/devops/maven-docker-agent:20220110',
            alwaysPullImage: true,
            ttyEnabled: true,
            command: 'cat'
        ),
         containerTemplate(
             name: 'kubectl',
             image: 'swr.cn-east-3.myhuaweicloud.com/chanjue/devops/kubectl:v1.19.16',
             command: 'cat',
             ttyEnabled: true
         )
    ],
    volumes: [
      hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
	  hostPathVolume(mountPath: '/root/.m2/repository', hostPath: '/var/lib/docker/k8s-hostpath/mavenRepos')
    ]
)
{
    node(label) {

		properties(
			[
			    disableConcurrentBuilds(),
				parameters([
					booleanParam(name: 'pipe_build_jjr', defaultValue: true, description: 'shulan-jjr-gateway 项目构建'),
					booleanParam(name: 'pipe_build_user', defaultValue: true, description: 'unified-user-gateway 项目构建'),
					booleanParam(name: 'shulan_jjr_gateway', defaultValue: false, description: 'shulan-jjr-gateway 项目部署'),
					booleanParam(name: 'unified_user_gateway', defaultValue: false, description: 'unified-user-gateway 项目部署')
				])
			]
		)

        stage("pipe_build"){
                 branch: branchName, credentialsId: credentialsId, url: gitAddress
            container("maven") {
                 buildCommand = "mvn clean package install deploy -T 8 -B -U -DskipTests=true -Ddockerfile.skip=false -Dbranch=$branchName"

                 if(params.pipe_build_jjr == false){
                    buildCommand = buildCommand + " -pl '!shulan-jjr-gateway'"
                 }
                 if(params.pipe_build_user == false){
                    buildCommand = buildCommand + " -pl '!unified-user-gateway'"
                 }
                 echo "buildCommand $buildCommand"
                 sh "$buildCommand"
                 version_tag = sh returnStdout: true, script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout"
                 version_tag = version_tag.trim()
                 version_tag = version_tag + "-" + branchName
                 echo "version_tag: $version_tag"
            }
		}

        stage("pipe_deploy"){

            if(branchName == "hw-build"){
                namespace = "shulan-dev"
            }else if(branchName == "uat"){
                namespace = "shulan-uat"
            }

            if(params.shulan_jjr_gateway == true){
                container("kubectl") {
                    def image_tag = "swr.cn-east-3.myhuaweicloud.com/chanjue/shulan-jjr-gateway:$version_tag"
                    echo "set image_tag: $image_tag"
                    sh "kubectl set image deployment shulan-jjr-gateway shulan-jjr-gateway=$image_tag -n $namespace"
                    sh "kubectl set env deployment shulan-jjr-gateway RESTARTED_AT=$deployTime -n $namespace"
                    echo "deploy done !!!"
               }
            }

            if(params.unified_user_gateway == true){
                container("kubectl") {
                    def image_tag = "swr.cn-east-3.myhuaweicloud.com/chanjue/unified-user-gateway:$version_tag"
                    echo "set image_tag: $image_tag"
                    sh "kubectl set image deployment unified-user-gateway unified-user-gateway=$image_tag -n $namespace"
                    sh "kubectl set env deployment unified-user-gateway RESTARTED_AT=$deployTime -n $namespace"
                    echo "deploy done !!!"
               }
            }
        }

    }
}
