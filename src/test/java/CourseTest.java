import com.avaje.ebean.Model;
import model.Course;
import model.Teacher;
import org.avaje.agentloader.AgentLoader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by merrillm on 11/22/16.
 */
public class CourseTest {
    
    @Before
    public void setupEbean() {
        if (!AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent","debug=1;packages=model")) {
            System.out.println("ebean-agent not found in classpath - not dynamically loaded");
        }
    }
    
    @Test
    public void databaseTest() {
        Teacher teacher = new Teacher();
        teacher.name = "Robert Paulson";
        teacher.save();
    
        Course course = new Course();
        course.name = "Testing Course";
        course.teacher = teacher;
        course.save();
        
        Course fetchedCourse = new Model.Finder<Long, Course>(Course.class)
                .byId(course.id);
        
        System.out.println(teacher.courses());
        
        assertNotNull(fetchedCourse);
        
        assertEquals("Testing Course", fetchedCourse.name);
        assertEquals(teacher.id, fetchedCourse.teacher.id);
        
        course.delete();
        teacher.delete();
    }
    
}
