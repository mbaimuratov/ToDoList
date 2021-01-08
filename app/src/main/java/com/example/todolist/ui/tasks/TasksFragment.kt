package com.example.todolist.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.SortOrder
import com.example.todolist.data.Task
import com.example.todolist.databinding.FragmentTasksBinding
import com.example.todolist.util.exhaustive
import com.example.todolist.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)

        val tasksAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(
                0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                /**
                 * Called when ItemTouchHelper wants to move the dragged item from its old position to
                 * the new position.
                 *
                 *
                 * If this method returns true, ItemTouchHelper assumes `viewHolder` has been moved
                 * to the adapter position of `target` ViewHolder
                 * ([ ViewHolder#getAdapterPosition()][ViewHolder.getAdapterPosition]).
                 *
                 *
                 * If you don't support drag & drop, this method will never be called.
                 *
                 * @param recyclerView The RecyclerView to which ItemTouchHelper is attached to.
                 * @param viewHolder   The ViewHolder which is being dragged by the user.
                 * @param target       The ViewHolder over which the currently active item is being
                 * dragged.
                 * @return True if the `viewHolder` has been moved to the adapter position of
                 * `target`.
                 * @see .onMoved
                 */
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                /**
                 * Called when a ViewHolder is swiped by the user.
                 *
                 *
                 * If you are returning relative directions ([.START] , [.END]) from the
                 * [.getMovementFlags] method, this method
                 * will also use relative directions. Otherwise, it will use absolute directions.
                 *
                 *
                 * If you don't support swiping, this method will never be called.
                 *
                 *
                 * ItemTouchHelper will keep a reference to the View until it is detached from
                 * RecyclerView.
                 * As soon as it is detached, ItemTouchHelper will call
                 * [.clearView].
                 *
                 * @param viewHolder The ViewHolder which has been swiped by the user.
                 * @param direction  The direction to which the ViewHolder is swiped. It is one of
                 * [.UP], [.DOWN],
                 * [.LEFT] or [.RIGHT]. If your
                 * [.getMovementFlags]
                 * method
                 * returned relative flags instead of [.LEFT] / [.RIGHT];
                 * `direction` will be relative as well. ([.START] or [                   ][.END]).
                 */
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TasksEvent.ShowUndoDeleteMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragment2ToAddEditTaskFragment(null, "New Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddedTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragment2ToAddEditTaskFragment(event.task, "Edit task")
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_tasks -> {

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}