package api.work.profile.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "githubClient", url = "${github.base-url}")
public interface GitHubClient {

    @GetMapping("/user")
    Map<String, Object> getUser(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/search/issues")
    Map<String, Object> searchPullRequests(
        @RequestParam("q") String query,
        @RequestHeader("Authorization") String authHeader
    );

    @GetMapping("/repos/{owner}/{repo}/commits")
    Map<String, Object>[] getCommits(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestHeader("Authorization") String authHeader
    );
    
    @GetMapping("/user/repos")
    Map<String, Object>[] getUserRepos(@RequestHeader("Authorization") String authHeader);
}