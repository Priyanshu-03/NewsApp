package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.ui.adapter.ArticleAdapter
import com.example.newsapp.util.Resource
import com.example.newsapp.util.shareNews
import com.example.newsapp.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlin.random.Random


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: ArticleAdapter
    val TAG = "BreakingNewsFragment"

    private fun setupRecyclerView() {
        newsAdapter = ArticleAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        newsAdapter.setOnItemCLickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }

        newsAdapter.onSaveClickListener {
            if (it.id == null) {
                it.id = Random.nextInt(0, 1000);
            }

            viewModel.insertArticle(it)
            Snackbar.make(requireView(), "Saved", Snackbar.LENGTH_SHORT).show()
        }

        newsAdapter.onDeleteClickListener {
            viewModel.deleteArticle(it)
            Snackbar.make(requireView(), "Removed", Snackbar.LENGTH_SHORT).show()
        }

        newsAdapter.onShareNewsClick {
            shareNews(context, it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        setupRecyclerView()

        setViewModelObserver()
    }

    private fun setViewModelObserver() {
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { newsResponse ->
            when (newsResponse) {
                is Resource.Success -> {
                    shimmerFrameLayout.stopShimmerAnimation()
                    shimmerFrameLayout.visibility = View.GONE
                    //hideProgressBar()
                    newsResponse.data?.let { news ->
                        rvBreakingNews.visibility = View.VISIBLE
                        newsAdapter.differ.submitList(news.articles)
                    }
                }

                is Resource.Error -> {
                    shimmerFrameLayout.visibility = View.GONE
                    //hideProgressBar()
                    newsResponse.message?.let { message ->
                        Log.e(TAG, "Error :: $message")
                    }
                }

                is Resource.Loading -> {
                    shimmerFrameLayout.startShimmerAnimation()
                    //showProgressBar()
                }
            }
        })
/* viewModel.getBreakingNews().observe(viewLifecycleOwner, Observer {
            if (it.size > 0) {
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
                rvBreakingNews.visibility = View.VISIBLE
                //newsAdapter.differ.submitList(it)
                newsAdapter.submitList(it)
                //newsAdapter.notifyDataSetChanged()
            }
        })*/
    }
}
