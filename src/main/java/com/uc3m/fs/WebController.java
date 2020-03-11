package com.uc3m.fs;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uc3m.fs.storage.StorageService;

@Controller
public class WebController {

	private static final String WS = "http://localhost:8091" + Config.PATH;

	private final StorageService storageService;

	@Autowired
	public WebController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping(value = "/")
	public String listUploadedFiles(Model model) throws IOException {
		model.addAttribute("files",
				storageService.loadAll()
				.map(path -> path.getFileName().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/download_file/{fileUuid}")
	public ResponseEntity<Resource> downloadFile(@PathVariable(value = "fileUuid", required = true) String uuid) {
		Resource file = storageService.loadAsResource(uuid);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}


	@GetMapping("/download/{fileUuid}")
	public String download(
			@PathVariable(value = "fileUuid", required = true) String uuid,
			RedirectAttributes redirectAttributes) {

		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(WS + "download/" + uuid, String.class);

		redirectAttributes.addFlashAttribute("file", "File: " + result);
		return "redirect:/";
	}

	@PostMapping("/upload")
	public String upload(
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = true) String dzuuid,
			@RequestParam(name = "List<site>", required = true) String[] sites,
			RedirectAttributes redirectAttributes) throws IOException {

		/*HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(new File(file.getOriginalFilename())));
		body.add("dzuuid", dzuuid);
		body.add("List<site>", sites);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result = restTemplate.postForEntity(WS + "upload", requestEntity, String.class);

		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded: " + result);*/

		String msg = null;
		try {
			storageService.store(file, dzuuid);
			msg = "You successfully uploaded " + file.getOriginalFilename() + "!";
		} catch (Exception e) {
			msg = "Error: " + e.getMessage();
		} finally {
			if (msg != null) redirectAttributes.addFlashAttribute("error_message", msg);
		}
		return "redirect:/";
	}

}