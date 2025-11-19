package com.example.scanner.presentetion

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanner.R
import com.example.scanner.data.QrAllHistoryDatabase
import com.example.scanner.data.QrHistoryItem
import com.example.scanner.databinding.FragmentHistoryBinding
import org.json.JSONObject

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    lateinit var db: QrAllHistoryDatabase
    lateinit var adapter: HistoryAdapter
    lateinit var favoritesHistoryAdapter: FavoritesHistoryAdapter
    val favoriteList = mutableListOf<QrHistoryItem>()
    var list = mutableListOf<QrHistoryItem>()
    var allList = mutableListOf<QrHistoryItem>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        db = QrAllHistoryDatabase(requireContext())

         list = db.getAllQrData().toMutableList()

        allList.clear()
        allList.addAll(list.filter { !it.isFavorite })


        adapter = HistoryAdapter(allList) { clickedItem ->
            onHistoryItemClick(clickedItem)
        }

        binding.rlHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rlHistory.adapter = adapter

        binding.ivDelete.setOnClickListener {
            if (adapter.deleteMode) {
                val selected = adapter.getSelectedItems()
                if (selected.isNotEmpty()) {
                    showDeleteConfirmDialog(selected)
                } else {
                    adapter.enableDeleteMode(false)
                    binding.allCheck.visibility = View.GONE
                    binding.allCheck.isChecked = false
                }
            } else {
                adapter.enableDeleteMode(true)
                binding.allCheck.visibility = View.VISIBLE
            }
        }

        binding.allCheck.setOnCheckedChangeListener { _, isChecked ->
            if (adapter.deleteMode) {
                if (isChecked) {
                    adapter.selectAll()
                } else {
                    adapter.clearSelection()
                }
            }
        }

        favoriteList.clear()
        favoriteList.addAll(list.filter { it.isFavorite })

        if (favoriteList.size > 3){
            binding.llViewAll.visibility = View.VISIBLE
        }else{
            binding.llViewAll.visibility = View.GONE
        }

        favoritesHistoryAdapter = FavoritesHistoryAdapter(favoriteList) { clickedItem ->
            onHistoryItemClick(clickedItem)
        }

        binding.rlHistoryFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rlHistoryFavorites.adapter = favoritesHistoryAdapter

        binding.llViewAll.setOnClickListener {
            val intent = Intent(requireActivity(), FavoriteHistoryActivity::class.java)
            startActivity(intent)
        }
        updateEmptyState()
        return binding.root
    }

    private fun showDeleteConfirmDialog(selected: Set<Long>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_history, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnYes = dialogView.findViewById<View>(R.id.btnYes)
        val btnCancel = dialogView.findViewById<View>(R.id.tvNo)

        btnCancel.setOnClickListener {
            dialog.dismiss()
            adapter.enableDeleteMode(false)

            binding.allCheck.visibility = View.GONE
            binding.allCheck.isChecked = false
        }

        btnYes.setOnClickListener {
            selected.forEach { id -> db.deleteItem(id) }

            adapter.removeByIds(selected)

            favoriteList.removeAll { selected.contains(it.id) }
            favoritesHistoryAdapter.notifyDataSetChanged()

            updateEmptyState()
            binding.allCheck.visibility = View.GONE
            binding.allCheck.isChecked = false
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (adapter.deleteMode) {
                adapter.enableDeleteMode(false)
                binding.allCheck.visibility = View.GONE
                binding.allCheck.isChecked = false
            }
        }

        dialog.show()
    }

    private fun updateEmptyState() {

        val hasFavorites = favoriteList.isNotEmpty()
        val hasUnFavorites = allList.isNotEmpty()

        if (!hasFavorites && !hasUnFavorites) {
            binding.llEmptyView.visibility = View.VISIBLE

            binding.cvHistoryFavorites.visibility = View.GONE
            binding.tvFavorites.visibility = View.GONE
            binding.rlHistory.visibility = View.GONE
            binding.ivDelete.visibility = View.GONE
            binding.llViewAll.visibility = View.GONE

        } else {
            binding.llEmptyView.visibility = View.GONE

            if (hasFavorites) {
                binding.cvHistoryFavorites.visibility = View.VISIBLE
                binding.tvFavorites.visibility = View.VISIBLE
                binding.llViewAll.visibility =
                    if (favoriteList.size > 3) View.VISIBLE else View.GONE
            } else {
                binding.cvHistoryFavorites.visibility = View.GONE
                binding.tvFavorites.visibility = View.GONE
                binding.llViewAll.visibility = View.GONE
            }

            binding.rlHistory.visibility = if (hasUnFavorites) View.VISIBLE else View.GONE

            binding.ivDelete.visibility = if (hasUnFavorites) View.VISIBLE else View.GONE
        }
    }

    private fun onHistoryItemClick(item: QrHistoryItem) {
        val json = JSONObject(item.jsonData)
        when (item.qrType) {
            "URL" -> {
                val intent = Intent(requireContext(), QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_url", item.value)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_type",item.qrType)
                startActivity(intent)
            }

            "Text" -> {
                val intent = Intent(requireContext(), QRTextContactActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_text", item.value)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_type",item.qrType)
                startActivity(intent)
            }

            "Email" -> {
                val intent = Intent(requireActivity(), QRPhoneEmailDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.value)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_email_to",json.optString("address"))
                intent.putExtra("qr_subject", json.optString("subject"))
                intent.putExtra("qr_body", json.optString("body"))
                startActivity(intent)
            }

            "Phone" -> {
                val intent = Intent(requireContext(), QRPhoneEmailDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_number", json.optString("number"))
                startActivity(intent)
            }

            "Contact" -> {
                val intent = Intent(requireContext(), QRTextContactActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_name", json.optString("name"))
                intent.putExtra("qr_organization", json.optString("organization"))
                intent.putExtra("qr_title", json.optString("title"))
                intent.putExtra("qr_address", json.optString("address"))
                intent.putExtra("qr_phone", json.optString("phone"))
                intent.putExtra("qr_email", json.optString("email"))
                startActivity(intent)
            }

            "WiFi" -> {
                val intent = Intent(requireContext(), QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_history_id", item.id)
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                intent.putExtra("qr_ssid", json.optString("ssid"))
                intent.putExtra("qr_password", json.optString("password"))
                intent.putExtra("qr_encryptionType", json.optString("encryptionType"))
                startActivity(intent)
            }


            else -> {
                val intent = Intent(requireContext(), QRUrlDetailsActivity::class.java)
                intent.putExtra("qr_raw", json.optString("rawValue"))
                intent.putExtra("qr_type", item.qrType)
                intent.putExtra("qr_image", item.qrImage)
                startActivity(intent)
            }
        }
    }
    override fun onResume() {
        super.onResume()

        val updatedList = db.getAllQrData().toMutableList()

        list = updatedList

        allList.clear()
        allList.addAll(updatedList.filter { !it.isFavorite })
        adapter.updateList(allList.toMutableList())

        val updatedFavorites = updatedList.filter { it.isFavorite }

        favoriteList.clear()
        favoriteList.addAll(updatedFavorites)
        favoritesHistoryAdapter.notifyDataSetChanged()

        updateEmptyState()
        binding.llViewAll.visibility = if (favoriteList.size > 3) View.VISIBLE else View.GONE
    }
}