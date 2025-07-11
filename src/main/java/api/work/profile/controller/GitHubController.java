package api.work.profile.controller;

import api.work.profile.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;

    @GetMapping("/user")
    public Object getUser() {
        return gitHubService.getUser();
    }

    @GetMapping("/prs/{username}")
    public int getPRs(@PathVariable String username) {
        return gitHubService.getPullRequestCount(username);
    }

}