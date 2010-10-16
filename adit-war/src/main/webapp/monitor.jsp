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
	
	<table style="border: 1px solid #000; border-collapse: collapse;">
		<tr>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Component</b></td>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Status</b></td>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Time / Error</b></td>
		</tr>
		<tr>
			<td style="border: 1px solid #000;"><span style="margin: 6px;">saveDocument() </span></td>
			<td style="border: 1px solid #000;"><span style="margin: 6px;"><%=status%></span></td>
			<td style="border: 1px solid #000;">
				<span style="margin: 6px;">
				<%if(status == "OK") {
				
				%>
					<%=duration%> s
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
				</span>
			</td>
		</tr>
	</table>
	
</body>
</html>