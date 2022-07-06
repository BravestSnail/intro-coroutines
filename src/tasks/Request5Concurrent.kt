package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgReposCallSuspend(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()
    
    val deferreds = repos.map {repo ->
        async {
            service
                .getRepoContributorsCallSuspend(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    deferreds.awaitAll().flatten().aggregate()
}