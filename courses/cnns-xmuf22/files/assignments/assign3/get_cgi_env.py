#!/usr/bin/python3

import os

def get_cgi_env():
    i = 0
    print("Content-type:text/html\r\n\r\n")
    print("<html>\n")
	
    print("<head>\n")
    print("<title> CGI Envrionment  Variables</title>\n")
    print("</head>\n")
    
    print("<body>\n")
    print("<table border =\"0\" cellspacing=\"2\">")

    for env in os.environ.keys():
        print("<tr>")
        print("<td> %s </td>" %(env))
        print("<td>")
    # value = os.environ[env]
    # if NULL != value:
        print(os.environ[env])
    # else:
    #     print("Environment variable does not exist.")
	
        print("</td>")
        print("</tr>\n")
	
    print("</table>")
    print("</body>\n")
    print("</html>\n")

if __name__ == '__main__':
     get_cgi_env()