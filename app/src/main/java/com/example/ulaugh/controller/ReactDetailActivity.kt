package com.example.ulaugh.controller

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.adapter.ReactAdapter
import com.example.ulaugh.databinding.ActivityReactDetailBinding
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAMarker
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAMarkerHover
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAMarkerStates
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReactDetailActivity : AppCompatActivity() {
    private var _binding: ActivityReactDetailBinding? = null
    private val binding get() = _binding!!
    private var state: CollapsingToolbarLayoutState? = null
    private var postDetail: HomeRecyclerViewItem.SharePostData? = null

    private enum class CollapsingToolbarLayoutState {
        EXPANDED, COLLAPSED, INTERNEDIATE
    }

    //    private val authViewModel by activityViewModels<AuthViewModel>()
    @Inject
    lateinit var sharePref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityReactDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window: Window = window
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        initViews()
        setToolbarFun()
        setGraph()
        setAdapter()
        setAppBar()
    }

    private fun initViews() {
        if (intent != null)
            postDetail =
                Gson().fromJson(
                    intent.getStringExtra(Constants.POST),
                    object : TypeToken<HomeRecyclerViewItem.SharePostData>() {}.type
                )
        if (postDetail != null){
            Glide.with(this)
                .load(postDetail!!.image_url)
                .centerCrop()
                .fitCenter()
                .thumbnail(0.3f)
                .placeholder(R.drawable.seokangjoon)
                .into(binding.profileIv)
            binding.included.postDetail.text = postDetail!!.description
            binding.included.trendingTv.text = postDetail!!.tagsList
            binding.countReact.text = Helper.prettyCount(postDetail!!.reaction!!.size)
        }
        binding.emoji.text = "U+1F970 U+1F602 U+1F92A"
    }

    private fun setAdapter() {
        val adapter = ReactAdapter(this)
        object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.included.rv.adapter = adapter
    }

    private fun setGraph() {

        val arrayList1 = arrayListOf<Double>()
        arrayList1.add(0.0)
        arrayList1.add(30.0)
        arrayList1.add(40.0)
        arrayList1.add(20.0)
        arrayList1.add(90.0)
//        if (notes.bbm.isEmpty())
//            noBpmTv.visibility = View.VISIBLE
//        for (i in 0 until notes.bbm.size) {
//            arrayList1.add(notes.bbm[i].bbm)
//        }
        val aaChartView = findViewById<AAChartView>(R.id.graph)

        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Areaspline)
            .animationDuration(3000)
            .gradientColorEnable(true)
//            .backgroundColor(getColor(R.color.light_purple))
            .stacking(AAChartStackingType.Normal)
            .markerRadius(0f)
            .yAxisTitle("")
            .yAxisLineWidth(0f)
            .yAxisGridLineWidth(2f)
            .xAxisGridLineWidth(0f)
            .xAxisVisible(false)
            .xAxisLabelsEnabled(false)
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("React Details")
                        .lineWidth(2.0f)
                        .color("rgb(69,101,207)")
                        .marker(
                            AAMarker()
                                .states(
                                    AAMarkerStates()
                                        .hover(
                                            AAMarkerHover()
                                                .enabled(false)
                                        )
                                )
                        )
                        .data(
                            Array(arrayList1.size) { i -> (arrayList1[i]) }
                        )
                )
            )

        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

    private fun setAppBar() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset == 0) {
                if (state != CollapsingToolbarLayoutState.EXPANDED) {
                    state =
                        CollapsingToolbarLayoutState.EXPANDED
//                    binding.collapsingToolbar.title = "EXPANDED"
                    //                        danmakuView.resume()
                }
            } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                if (state != CollapsingToolbarLayoutState.COLLAPSED) {
//                    binding.toolTxt.setText("google")
//                    binding.hideShowAppbar.visibility = View.VISIBLE
//                    binding.toolbarImage.setImageResource(R.drawable.ic_lock_lock)
                    binding.toolbar.title = "google"
                    Glide.with(this)
                        .load(postDetail!!.image_url)
                        .centerCrop()
                        .fitCenter()
                        .thumbnail(0.3f)
                        .placeholder(R.drawable.seokangjoon)
                        .into(binding.profileIv)
                    state =
                        CollapsingToolbarLayoutState.COLLAPSED
                    //                        danmakuView.pause()
                }
            } else {
                if (state != CollapsingToolbarLayoutState.INTERNEDIATE) {
                    if (state == CollapsingToolbarLayoutState.COLLAPSED) {
//                        binding.hideShowAppbar.visibility = View.GONE
                        binding.toolbar.title = "google"
                    }
//                    binding.collapsingToolbar.setTitle("INTERNEDIATE")
                    state =
                        CollapsingToolbarLayoutState.INTERNEDIATE
                }
            }
        }
    }

    private fun setToolbarFun() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}