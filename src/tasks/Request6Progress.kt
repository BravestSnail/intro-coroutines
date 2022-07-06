package tasks

import contributors.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgReposCallSuspend(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()
        var contributors = emptyList<User>()
        val count = AtomicInteger(0)

        repos.map {repo ->
            launch {
                val users = service
                    .getRepoContributorsCallSuspend(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                contributors = (contributors + users).aggregate()
                updateResults(contributors,count.getAndIncrement() == repos.lastIndex)
            }
        }

    }
}
