package com.kalios.quotesgenerator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuoteAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // Initialize the RecyclerView and set its layout manager
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list and set it to the RecyclerView
        adapter = QuoteAdapter(ArrayList())
        recyclerView.adapter = adapter

        // Initialize the database
        db = AppDatabase.getDatabase(this)

        // Fetch favorite quotes from the database
        fetchFavoriteQuotes()

        // Implement swipe-to-delete functionality using ItemTouchHelper
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // We do not want to handle moving items in this implementation
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Get the position of the swiped item
                val position = viewHolder.adapterPosition

                // Get the deleted quote object
                val deletedQuote = adapter.getData()[position]

                // Remove the item from the adapter
                adapter.removeItem(position)
                GlobalScope.launch(Dispatchers.IO) {
                    db.quoteDao().delete(deletedQuote)
                }
                    // Show a Snackbar with an undo option
                Snackbar.make(recyclerView, "Deleted ${deletedQuote.quote}", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // Restore the deleted item if the user clicks undo
                        adapter.restoreItem(deletedQuote, position)
                        GlobalScope.launch(Dispatchers.IO) {
                            db.quoteDao().insert(deletedQuote)
                        }
                    }.show()
            }
        }).attachToRecyclerView(recyclerView) // Attach the ItemTouchHelper to the RecyclerView
    }

    private fun fetchFavoriteQuotes() {
        // Fetch favorite quotes from the database in a background thread
        GlobalScope.launch(Dispatchers.IO) {
            val favoriteQuotes = db.quoteDao().getAllQuotes()
            withContext(Dispatchers.Main) {
                // Update the adapter with the fetched quotes on the main thread
                adapter.setQuotes(ArrayList(favoriteQuotes))
            }
        }
    }
}
