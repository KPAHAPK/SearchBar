package com.example.searchbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.searchbar.databinding.FragmentSearchBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SearchFragment: Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding
        get() = _binding!!

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    companion object{
        fun newInstance() = SearchFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSearchStateFlow()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        _binding = null
    }

    private fun dataFromNetwork(query: String): Flow<String> {
        return flow {
            delay(2000)
            emit(query)
        }
    }

    private fun setUpSearchStateFlow(){
        scope.launch {
            binding.searchView.getQueryTextChangeStateFlow()
                .debounce(500)
                .filter { query ->
                    return@filter query.isNotEmpty()
                }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    dataFromNetwork(query)
                        .catch {
                            emit("")
                        }
                }
                .collect { result -> binding.resultTextView.text = result }
        }


    }

    private fun SearchView.getQueryTextChangeStateFlow(): StateFlow<String> {
        val queryStateFlow = MutableStateFlow("")

        setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let{queryStateFlow.value = it}
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                queryStateFlow.value = newText!!
                return true
            }
        })

        return queryStateFlow
    }
}