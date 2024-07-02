package com.kalios.quotesgenerator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuoteAdapter(private var quotes: ArrayList<Quote>) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    // ViewHolder class to hold references to the views
    class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteTextView: TextView = itemView.findViewById(R.id.quote_text)
        val authorTextView: TextView = itemView.findViewById(R.id.author_text)
    }

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    // Bind the data to the views
    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        holder.quoteTextView.text = quote.quote
        holder.authorTextView.text = quote.author
    }

    // Return the total count of items
    override fun getItemCount() = quotes.size

    // Update the list of quotes and notify the adapter
    fun setQuotes(quotes: ArrayList<Quote>) {
        this.quotes = quotes
        notifyDataSetChanged()
    }

    // Remove an item from the list and notify the adapter
    fun removeItem(position: Int) {
        quotes.removeAt(position)
        notifyItemRemoved(position)
    }

    // Restore an item to the list and notify the adapter
    fun restoreItem(item: Quote, position: Int) {
        quotes.add(position, item)
        notifyItemInserted(position)
    }

    // Get the current list of quotes
    fun getData(): ArrayList<Quote> = quotes
}
