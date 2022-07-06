package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
        val repos = service
            .getOrgReposCallSuspend(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

        val deferreds = repos.map {repo ->
            GlobalScope.async(Dispatchers.Default) {
                log("starting loading for ${repo.name}")
                delay(3000)
                service
                    .getRepoContributorsCallSuspend(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }
        return deferreds.awaitAll().flatten().aggregate()
}
