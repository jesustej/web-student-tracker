package com.web.jdbc;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class StudentController
 */
@WebServlet("/StudentController")
public class StudentController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private StudentDbUtil studentDbUtil;

	@Resource(name = "jdbc/web_student_tracker") // Resource Injection by tomcat to this DataSource
	private DataSource dataSource;

	// In Servlet lifecycle there is a special method called init() where
	// we can initialize our studentDbUtil class.
	@Override
	public void init() throws ServletException {
		// create our student db util ... and pass in the conn pool / datasource
		try {
			studentDbUtil = new StudentDbUtil(dataSource);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			// read the "command" parameter
			String theCommand = request.getParameter("command");

			// if the command is missing, then default to listing students
			if (theCommand == null) {
				theCommand = "LIST";
			}

			// route to the appropriate method
			switch (theCommand) {
			case "LIST":
				listStudents(request, response);
				break;
			case "LOAD":
				loadStudent(request, response);
				break;
			case "DELETE":
				deleteStudent(request,response);
				break;
			case "SEARCH":
				searchStudent(request,response);
				break;
			default:
				listStudents(request, response);
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	

	private void searchStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String searchName = request.getParameter("theSearchName");
		// search students from db util
        List<Student> students = studentDbUtil.searchStudents(searchName); 
        
        // add students to the request
        request.setAttribute("STUDENT_LIST", students);
                
        // send to JSP page (view)
        RequestDispatcher dispatcher = request.getRequestDispatcher("/list-students.jsp");
        dispatcher.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            // read the "command" parameter
            String theCommand = request.getParameter("command");
                    
            // route to the appropriate method
            switch (theCommand) {
                            
            case "ADD":
                addStudent(request, response);
                break;
            case "UPDATE":
				updateStudent(request, response);
				break;                   
            default:
                listStudents(request, response);
            }
                
        }
        catch (Exception exc) {
            throw new ServletException(exc);
        }
	}

	private void deleteStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// read student info from form data
				int id = Integer.parseInt(request.getParameter("studentId"));
				
				// add the student to the database
				studentDbUtil.deleteStudent(id);
				
				// send back to main page (the student list)
				listStudents(request, response);
	}

	private void updateStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// read student info from form data
		int id = Integer.parseInt(request.getParameter("studentId"));
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		String email = request.getParameter("email");
		
		// create new student object
		Student theStudent = new Student(id, firstName, lastName, email);
		
		// perform update on the database
		studentDbUtil.updateStudent(theStudent);
		
		
		response.sendRedirect(request.getContextPath() + "/StudentController?command=LIST");
	}

	private void loadStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Get the id from form data
		String thestudentId = request.getParameter("studentId");

		// get student form database
		Student theStudent = studentDbUtil.getStudent(thestudentId);

		// place student in the request attribute
		request.setAttribute("THE_STUDENT", theStudent);

		// sending this to jsp page: update-student-form.jsp
		RequestDispatcher dispatcher = request.getRequestDispatcher("/update-student-form.jsp");
		dispatcher.forward(request, response);

	}

	private void addStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// read student info from form data
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		String email = request.getParameter("email");

		// create new student object
		Student theStudent = new Student(firstName, lastName, email);
		// add the student to the database
		studentDbUtil.addStudent(theStudent);

        // SEND AS REDIRECT to avoid multiple-browser reload issue
        response.sendRedirect(request.getContextPath() + "/StudentController?command=LIST");
	}

	private void listStudents(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// get students from db util
		List<Student> students = studentDbUtil.getStudents();

		// add students to the request
		request.setAttribute("STUDENT_LIST", students);

		// send to JSP page (view)
		RequestDispatcher dispatcher = request.getRequestDispatcher("/list-students.jsp");
		dispatcher.forward(request, response);
	}

}
