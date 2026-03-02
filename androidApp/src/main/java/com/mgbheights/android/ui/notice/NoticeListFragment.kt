package com.mgbheights.android.ui.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentNoticeListBinding
import com.mgbheights.android.ui.adapter.NoticeAdapter
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeListFragment : Fragment() {

    private var _binding: FragmentNoticeListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoticeViewModel by viewModels()
    private lateinit var noticeAdapter: NoticeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNoticeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noticeAdapter = NoticeAdapter { notice ->
            viewModel.markRead(notice.id)
            val action = NoticeListFragmentDirections.actionNoticeListToNoticeDetail(notice.id)
            findNavController().navigate(action)
        }
        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = noticeAdapter

        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadNotices() }

        binding.fabCreateNotice.setOnClickListener {
            findNavController().navigate(R.id.action_noticeList_to_createNotice)
        }

        viewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            binding.fabCreateNotice.isVisible = isAdmin
        }

        viewModel.notices.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.progressLoading.isVisible = true
                is Resource.Success -> {
                    binding.progressLoading.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                    noticeAdapter.submitList(state.data)
                    binding.layoutEmpty.isVisible = state.data.isEmpty()
                }
                is Resource.Error -> {
                    binding.progressLoading.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                    binding.layoutEmpty.isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

