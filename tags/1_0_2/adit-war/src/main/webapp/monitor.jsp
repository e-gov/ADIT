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
	
	<table style="border: 1px solid #000; border-collapse: collapse;">
		<tr>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Component</b></td>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Status</b></td>
			<td style="border: 1px solid #000;"><b style="margin: 6px;">Time / Error</b></td>
		</tr>
		
		<%
			for(int i = 0; i < results.size(); i++) {
				MonitorResult result = results.get(i);
		%>
		
			<tr>
				<td style="border: 1px solid #000;"><span style="margin: 6px;"><%=result.getComponent()%></span></td>
				<td style="border: 1px solid #000;"><span style="margin: 6px;"><%=result.getStatusString()%></span></td>
				<td style="border: 1px solid #000;">
					<span style="margin: 6px;">
					<%if(result.isSuccess()) {
					
					%>
						<%=result.getDurationString()%> s
					<%
					  } else {
					%>
						<%=result.getExceptionString()%>
					<%
					  }
					%>
					</span>
				</td>
			</tr>
			
		<%
			}
		%>
			
		
		
	</table>
	
</body>
</html>