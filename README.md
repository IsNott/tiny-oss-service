# 基于minio的oss私服
# A tiny oss system base on Minio

## 如何使用
- 在数据库中创建SysMinioFile表，建表语句在createtable.txt中
- bootstrap.yml中配置nacos地址
- nacos添加名称为tiny-oss-service的命名空间，创建tiny-oss-service-dev.yaml配置文件
- 在配置文件中修改datasource、minio地址，案例配置文件是resources/bootstrap-exapmle.yml



## 依赖
## dependency
  springboot 2.2.11
  spring-cloud-openfeign 2.2.6
  spring-cloud-starter-alibaba-nacos 2.1.0
  thumbnailator 0.4.9
