<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Adit Monitoring Application</title>
</head>

<% 
	String status = request.getParameter("test"); 
	String status2 = (String) request.getAttribute("test");
%>

<body>
	<h1>Adit Service</h1>
</body>
</html>