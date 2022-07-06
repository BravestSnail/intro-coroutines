package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
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
        val channel = Channel<List<User>>()
        repos.map {repo ->
            launch {
                val users = service
                    .getRepoContributorsCallSuspend(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }
        repeat(repos.size){
            contributors = (channel.receive() + contributors).aggregate()
            updateResults(contributors,it==repos.lastIndex)
        }
    }
}
