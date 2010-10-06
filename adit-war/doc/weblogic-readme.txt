When deploying in weblogic, the classloading is different. To get hibernate working you must add the
antlr-2.7.6.jar on the classpath before weblogic classes. This means that in your domain startup script:

EXAMPLE:  set PRE_CLASSPATH=E:\adit\WEB-INF\lib\antlr-2.7.6.jar;%PRE_CLASSPATH%