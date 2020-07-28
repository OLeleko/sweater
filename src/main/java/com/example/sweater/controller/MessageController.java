package com.example.sweater.controller;

import com.example.sweater.domain.Message;
import com.example.sweater.domain.User;
import com.example.sweater.domain.dto.MessageDto;
import com.example.sweater.repos.MessageRepo;
import com.example.sweater.service.MessageService;
import com.example.sweater.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class MessageController {
    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String greeting(Map<String, Object> model){
        return "greeting";
    }

    @GetMapping("/man")
    public String Man(@RequestParam(required = false, defaultValue = "") String filter,
                      Model model,
                      @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                      @AuthenticationPrincipal User user
    ){
        Page<MessageDto> page = messageService.messageList(pageable, filter, user);

        model.addAttribute("page", page);
        model.addAttribute("url", "/man");
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
            @RequestParam(name="message", required = false) Integer mid,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ){
        User author = userService.findById(id);
        Message message = messageRepo.findById(mid);
        Page<MessageDto> page = messageService.messageListForUser(pageable, currentUser, author);
        model.addAttribute("userChannel", author);
        model.addAttribute("subscriptionsCount", author.getSubscriptions().size());
        model.addAttribute("subscribersCount", author.getSubscribers().size());
        model.addAttribute("isSubscriber", author.getSubscribers().contains(currentUser));
        model.addAttribute("page", page);
        model.addAttribute("message", message);
        model.addAttribute("isCurrentUser", currentUser.equals(author));
        model.addAttribute("url", "/author/messages/" + author.getId());
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

    @GetMapping("/messages/{id}/like")
    public String like(
            @AuthenticationPrincipal User currentUser,
            @PathVariable int id,
            RedirectAttributes redirectAttributes,
            @RequestHeader(required = false) String referer
    ){
        Message message = messageRepo.findById(id);

        Set<User> likes = message.getLikes();
        if(likes.contains(currentUser)){
            likes.remove(currentUser);
        }else {
            likes.add(currentUser);
        }

        UriComponents components = UriComponentsBuilder.fromHttpUrl(referer).build();
        components.getQueryParams()
                .entrySet()
                .forEach(pair -> redirectAttributes.addAttribute(pair.getKey(), pair.getValue()));

        return "redirect:" + components.getPath();
    }
}


