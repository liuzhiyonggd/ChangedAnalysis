package sysu.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import net.sf.json.JSONObject;
import sysu.commconsistency.inserter.ClassMessageInserter;
import sysu.commconsistency.inserter.CommentInserter;
import sysu.common.util.AnalysisFileUtils;
import sysu.coreclass.inserter.ClassInserter;
import sysu.database.repository.UserRepository;
import sysu.web.bean.ChartData;
import sysu.web.bean.Data;
import sysu.web.bean.User;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model,HttpServletRequest request) {

		ChartData chartData = new ChartData();
		chartData.setLabels(new ArrayList<String>());
		List<Data> datasets = new ArrayList<Data>();
		Data data = new Data();
		data.setData(new ArrayList<Double>());
		data.setBackgroundColor("rgba(151,187,205,1)");
		data.setBorderColor("rgba(151,187,205,0.8)");
		datasets.add(data);
		chartData.setDatasets(datasets);
		JSONObject jsonChartData = JSONObject.fromObject(chartData);
		model.addAttribute("chartData", jsonChartData);
		return "index";
	}

	@RequestMapping(value = "/signin", method = RequestMethod.POST)
	public String signin(@ModelAttribute User user, Model model) {

		String username = user.getUserName();
		if (userRepository.findByUserName(username) != null) {
			User t_user = new User();
			model.addAttribute("user", t_user);
			model.addAttribute("exist_username", true);
			return "signin";
		}
		List<String> roles = new ArrayList<String>();
		roles.add("USER");
		user.setRoles(roles);
		userRepository.insert(user);
		return "login";
	}

	@RequestMapping(value = "/signin", method = RequestMethod.GET)
	public String signin(Model model) {
		User user = new User();
		model.addAttribute("user", user);
		model.addAttribute("exist_username", false);
		return "signin";
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Model model) {
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails) {

			return "index";
		} else {
			return "login";
		}
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public void upload(@RequestParam("fileToUpload") MultipartFile file, HttpServletRequest request)
			throws IOException {
		System.out.println("call upload.");
		String fileName = file.getOriginalFilename();
		System.out.println(fileName);
		
		FileUtils.writeByteArrayToFile(new File("/home/angel/changeanalysis/"+fileName), file.getBytes());
		AnalysisFileUtils.unZip("/home/angel/changeanalysis/" + fileName, "/home/angel/changeanalysis/files/user/" + fileName);
		int versionID = ClassInserter.insert("/home/angel/changeanalysis/files/user/" + fileName + "/old",
				"/home/angel/changeanalysis/files/user/" + fileName + "/new", "liuzhiyong");
		
		ClassMessageInserter.insertVersion("/home/angel/changeanalysis/files/user/" + fileName, versionID);
		CommentInserter.insert(versionID);
		request.getSession().setAttribute("versionID", versionID);
		

	}
	
	
}