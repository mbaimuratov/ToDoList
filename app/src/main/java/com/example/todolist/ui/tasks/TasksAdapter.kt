package com.example.todolist.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.Task
import com.example.todolist.databinding.ItemTaskBinding

class TasksAdapter(private val listener: OnItemClickListener) : ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }

                checkBoxCompleted.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, checkBoxCompleted.isChecked)
                    }
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                checkBoxCompleted.isChecked = task.completed
                textViewName.text = task.name
                textViewName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        /**
         * Called to check whether two objects represent the same item.
         *
         *
         * For example, if your items have unique ids, this method should check their id equality.
         *
         *
         * Note: `null` items in the list are assumed to be the same as another `null`
         * item and are assumed to not be the same as a non-`null` item. This callback will
         * not be invoked for either of those cases.
         *
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return True if the two items represent the same object or false if they are different.
         *
         * @see Callback.areItemsTheSame
         */
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Called to check whether two items have the same data.
         *
         *
         * This information is used to detect if the contents of an item have changed.
         *
         *
         * This method to check equality instead of [Object.equals] so that you can
         * change its behavior depending on your UI.
         *
         *
         * For example, if you are using DiffUtil with a
         * [RecyclerView.Adapter], you should
         * return whether the items' visual representations are the same.
         *
         *
         * This method is called only if [.areItemsTheSame] returns `true` for
         * these items.
         *
         *
         * Note: Two `null` items are assumed to represent the same contents. This callback
         * will not be invoked for this case.
         *
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return True if the contents of the items are the same or false if they are different.
         *
         * @see Callback.areContentsTheSame
         */
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}