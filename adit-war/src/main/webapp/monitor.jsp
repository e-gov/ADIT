<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="java.util.List" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Adit Monitoring Application</title>
</head>

<%  
	String duration = (String) request.getAttribute("duration");
	List<String> exceptions = (List<String>) request.getAttribute("exceptions");
	String status = (String) request.getAttribute("status");
%>

<body>
	<h1>Adit Monitoring</h1>
	
	<table style="border: 1px solid #000; border-collapse: collapse; padding: 6px;">
		<tr>
			<td style="border: 1px solid #000;"><b>Component</b></td>
			<td style="border: 1px solid #000;"><b>Status</b></td>
			<td style="border: 1px solid #000;"><b>Time / Error</b></td>
		</tr>
		<tr>
			<td style="border: 1px solid #000;">saveDocument() request</td>
			<td style="border: 1px solid #000;"><%=status%></td>
			<td style="border: 1px solid #000;">
				<%if(status == "OK") {
				
				%>
					<%=duration%> ms
				<%
				  } else {
					  for(int i = 0; i < exceptions.size(); i++) {
						String exception = exceptions.get(i);						  
					  	if(i > 0)
					  		exception = ", " + exception;
				%>
					
					
					<%=exception%>
				<%
					  }
				  }
				%>
			</td>
		</tr>
	</table>
	
</body>
</html>