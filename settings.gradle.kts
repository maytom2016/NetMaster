pluginManagement {
    repositories {
        // github action机器在国外不需要阿里云镜像
        //maven { setUrl("https://maven.aliyun.com/repository/central") }
        //maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        //maven { setUrl("https://maven.aliyun.com/repository/google") }
        //maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        //maven { setUrl("https://maven.aliyun.com/repository/public") }

        maven { setUrl("https://jitpack.io") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // github action机器在国外不需要阿里云镜像
        //maven { setUrl("https://maven.aliyun.com/repository/central") }
        //maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        //maven { setUrl("https://maven.aliyun.com/repository/google") }
        //maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        //maven { setUrl("https://maven.aliyun.com/repository/public") }

        maven { setUrl("https://jitpack.io") }
        google()
        mavenCentral()
    }
}

rootProject.name = "NetMaster"
include(":app")
 
