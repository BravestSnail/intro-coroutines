package tasks

import contributors.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
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
        val contributors = CopyOnWriteArrayList<User>()
        val count = AtomicInteger(0)

        repos.map {repo ->
            launch {
                contributors += service
                    .getRepoContributorsCallSuspend(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                updateResults(contributors.aggregate(),count.getAndIncrement() == repos.lastIndex)
            }
        }

    }
}
