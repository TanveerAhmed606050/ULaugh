package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterReactLayoutBinding
import com.example.ulaugh.model.Emoji
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class ReactAdapter(
    private val reactionList: List<Emoji>,
    val totalReaction: Int,
    var context: Context
) :
    RecyclerView.Adapter<ReactAdapter.ViewHolder>() {
    private var _binding: AdapterReactLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding =
            AdapterReactLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(binding: AdapterReactLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reactionDetail = reactionList[position]
        setEmotions(reactionDetail)
//        when (position) {
//            0 -> {
//                binding.reactName.text = "${reactionDetail.name} Reacts"
//                when (reactionDetail.name) {
//                    "happy" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                    }
//                    "angry" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                    }
//                    "disgust" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                    "neutral" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                    }
//                    "fear" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "surprise" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "sad" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                }
//            }
//            1 -> {
//                when (reactionDetail.name) {
//                    "happy" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                    }
//                    "angry" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                    }
//                    "disgust" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                    "neutral" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                    }
//                    "fear" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "surprise" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "sad" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                }
//            }
//            2 -> {
//                when (reactionDetail.name) {
//                    "happy" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                    }
//                    "angry" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                    }
//                    "disgust" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                    "neutral" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                    }
//                    "fear" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "surprise" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "sad" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                }
//            }
//            3 -> {
//                when (reactionDetail.name) {
//                    "happy" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                    }
//                    "angry" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                    }
//                    "disgust" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                    "neutral" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                    }
//                    "fear" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "surprise" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "sad" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                }
//            }
//            4 -> {
//                when (reactionDetail.name) {
//                    "happy" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                    }
//                    "angry" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                    }
//                    "disgust" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                    "neutral" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                    }
//                    "fear" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "surprise" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                    }
//                    "sad" -> {
//                        binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                    }
//                }
//            }
//            5 -> {
//
//            }
//            6 -> {
//
//            }
//        val inbox = reactList[position]
    }


    private fun setEmotions(reactionDetail: Emoji) {
        val percent = ((reactionDetail.count.toDouble() * 100) / totalReaction.toDouble()).roundToInt()
        binding.reactPer.text = "$percent%"
        binding.reactName.text = reactionDetail.name
        when (reactionDetail.name) {
            "happy" -> {
                binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
            }
            "angry" -> {
                binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
            }
            "disgust" -> {
                binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
            }
            "neutral" -> {
                binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
            }
            "fear" -> {
                binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
            }
            "surprise" -> {
                binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
            }
            "sad" -> {
                binding.mostEmoji.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
            }
        }
    }


    //    private fun getEmojiByUnicode(unicode: Int): String {
//        return String(Character.toChars(unicode))
//    }
    override fun getItemCount(): Int {
        return reactionList.size
    }
}