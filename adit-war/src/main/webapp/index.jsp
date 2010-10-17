<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Adit Service</title>
</head>

<%
	String requestURL = "/adit";
	try {
		requestURL = request.getRequestURL().toString();
	} catch (Exception e) {
		;
	}
%>

<body>
	<h1>Adit Service</h1>
	
	<table style="border: 1px solid #000; border-collapse: collapse;">
		<tr>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Web-Service</b></td>
			<td style="border: 1px solid #000;"><span style="margin: 6px;"><%=requestURL%>/service</span></td>
		</tr>
		<tr>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Monitor</b></td>
			<td style="border: 1px solid #000;"><span style="margin: 6px;"><%=requestURL%>/monitor</span></td>
		</tr>
	</table>
	
</body>
</html>