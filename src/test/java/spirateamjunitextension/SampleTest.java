package spirateamjunitextension;

import org.junit.Rule;
import org.junit.Test;

import com.inflectra.spiratest.addons.junitextension.SpiraTestCase;
import com.inflectra.spiratest.addons.junitextension.SpiraTestConfiguration;
import com.inflectra.spiratest.addons.junitextension.SpiraTestWatchman;


@SpiraTestConfiguration () // you can set variables in this, but to be dynamic you set them on the run time. with -D args.
//-DEnableSpiraReporting=true //true or false // enables or disables spirateam link
//-DSpiraUrl=https://spirateam.com //url to root of spirateam.
//-DSpiraLogin=uploader //username of the auto test runner
//-DSpiraPassword=123uploader //password of the auto test runner 
//-DSpiraProjectId=1 //ProjectId where test is located
//-DSpiraReleaseId=1 //ReleaseId in project is optional but is useful to run tests in an iteration or next version.
public class SampleTest {
	
	@Rule
	public SpiraTestWatchman spiraTestWatchman = new SpiraTestWatchman();   //Required else it won't report at the end of all junit tests
	
	@Test
	@SpiraTestCase(testCaseId=1)//testCaseId is the id number of the test case in spireaTeam, do not include any extra 0's on the front.
	public void test() {
	
	
	
	}

}
