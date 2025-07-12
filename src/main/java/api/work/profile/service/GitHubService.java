package api.work.profile.service;

import api.work.profile.client.GitHubClient;
import api.work.profile.config.GitHubConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubService {

    private final GitHubClient gitHubClient;
    private final GitHubConfig gitHubConfig;

    public Map<String, Object> getUser() {
        return gitHubClient.getUser(getAuthHeader());
    }

    public int getPullRequestCount(String username) {
        try {
            String query = String.format("author:%s type:pr", username);
            log.debug("[GITHUB] Query PRs: {}", query);
            
            var response = gitHubClient.searchPullRequests(query, getAuthHeader());
            return (Integer) response.getOrDefault("total_count", 0);
        } catch (Exception e) {
            log.error("[GITHUB] Erro ao buscar PRs para {}: {}", username, e.getMessage());
            return 0;
        }
    }



    private String getAuthHeader() {
        return "token " + gitHubConfig.getToken();
    }
}