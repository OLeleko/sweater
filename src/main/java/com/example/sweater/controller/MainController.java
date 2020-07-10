package com.example.sweater.controller;

import com.example.sweater.domain.Message;
import com.example.sweater.domain.User;
import com.example.sweater.repos.MessageRepo;
import com.example.sweater.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class MainController {
    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private UserService userService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String greeting(Map<String, Object> model){
        return "greeting";
    }

    @GetMapping("/man")
    public String Man(@RequestParam(required = false, defaultValue = "") String filter, Model model){
        Iterable<Message> messages = messageRepo.findAll();
        if(!filter.isEmpty() && filter != null ){
            messages = messageRepo.findByTag(filter);
        }else {
            messages = messageRepo.findAll();
        }
        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);
        return "man";
    }

    @PostMapping("/man")
    public String add(
            @AuthenticationPrincipal User user,
            @Valid Message message,
            BindingResult bindingResult,
            Model model,
            @RequestParam("file") MultipartFile file) throws IOException {
        message.setAuthor(user);
        if(bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errorsMap);
            model.addAttribute("message", message);
        }else {

            saveFile(message, file);

            model.addAttribute("message", null);
            messageRepo.save(message);
        }
        Iterable<Message> messages = messageRepo.findAll();
        model.addAttribute("messages", messages);
        //model.addAttribute("filter", "");

        return "man";

    }

    private void saveFile(@Valid Message message, @RequestParam("file") MultipartFile file) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + resultFilename));
            message.setFilename(resultFilename);
        }
    }

    @GetMapping("/user-messages/{id}")
    public String userMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable int id,
            Model model,
            @RequestParam(name="message", required = false) Integer mid
    ){
        User user = userService.findById(id);
        Message message = messageRepo.findById(mid);
        Set<Message> messages = user.getMessages();
        model.addAttribute("messages", messages);
        model.addAttribute("message", message);
        /*if(!message.getTag().isEmpty()){
            model.addAttribute(message.getTag());
        }
        if(!message.getFilename().isEmpty()){
            model.addAttribute(message.getFilename());
        }*/
        model.addAttribute("isCurrentUser", currentUser.equals(user));
        return "userMessages";
    }

    @PostMapping("/user-messages/{id}")
    public String updateMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable int id,
            @RequestParam("id") int mid,
            @RequestParam("text") String text,
            @RequestParam("tag") String tag,
            @RequestParam("file") MultipartFile file
            ) throws IOException {
        User user = userService.findById(id);
        Message message = messageRepo.findById(mid);

        if(message.getAuthor().equals(currentUser)){
            if(!StringUtils.isEmpty(text)){
                message.setText(text);
            }
            if(!StringUtils.isEmpty(tag)){
                message.setTag(tag);
            }

            saveFile(message, file);
            messageRepo.save(message);

        }
        return "redirect:/user-messages/" + currentUser.getId();

    }
}


