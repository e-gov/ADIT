<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="java.util.List" %>
<%@page import="java.util.ArrayList" %>
<%@page import="ee.adit.monitor.MonitorResult" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Adit Monitoring Application</title>
</head>

<%  

	List<MonitorResult> results = new ArrayList<MonitorResult>();
	try {
		results = (List<MonitorResult>) request.getAttribute("results");
	} catch (Exception e) {
		;
	}
%>

<body>
	<h1>Adit Monitoring</h1>
	
	<table border=1>
		<tr>
			<td><b>Component</b></td>
			<td><b>Status</b></td>
			<td><b>Time / Error</b></td>
		</tr>
		<%for(int i = 0; i < results.size(); i++) {
				MonitorResult result = results.get(i);
		%><tr><td><%=result.getComponent()%></td><td><%=result.getStatusString()%></td><td><%if(result.isSuccess()) {%><%=result.getDurationString()%> s<%} else {%><%=result.getExceptionString()%><%}%></td></tr>
		<%}%>
	</table>
	
</body>
</html>