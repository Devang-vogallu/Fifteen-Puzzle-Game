package com.ttt.fifteenpuzzlegame

import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class Puzzle15ViewModel : ViewModel() {

    private val _isGameReset = MutableLiveData(false)
    val isGameReset: LiveData<Boolean> = _isGameReset

    private val _isGameStarted = MutableLiveData(false)
    //val isGameStarted: LiveData<Boolean> = _isGameStarted

    private var counter: Int = 0

    private val _boardData = mutableListOf<CellInfo>()
    val boardData: List<CellInfo> = _boardData

    private val _shakeFlow = MutableSharedFlow<CellInfo>()
    val shakeFlow = _shakeFlow.asSharedFlow()

    //////////////////////////////////
    init {
        _boardData.clear()
        _boardData.addAll(createPuzzleData())
    }
//
//    ///////////////////////////
//    init {
//        setBoard()
//    }
//
//    private fun setBoard() {
//        _boardData.addAll(createPuzzleData())
//
//        viewModelScope.launch {
//            var i = 2
//            val emptyCell = boardData.find { it.number == 0 }!!
//            while (i >= 0) {
//                delay(10)
//                val random = boardData.filter {
//                    it != emptyCell && (it.actualColumn == emptyCell.actualColumn || it.actualRow == emptyCell.actualRow)
//                }.random()
//                onCellClicked(random)
//                i--
//            }
//        }
//  }

    //////////////////////////////
    private fun createPuzzleData(): List<CellInfo> {
        //val tempNumberList = (1..15).toList().shuffled()
        val tempNumberList = shufflePuzzle()
        return mutableListOf<CellInfo>().apply {
            for (num in 1..15) {
                add(
                    CellInfo(
                        number = tempNumberList[num - 1],
                        row = (num - 1) / 4,
                        column = (num - 1) % 4,
                        size = 0 // must be set after measurement
                    )
                )
            }
            add(
                CellInfo(
                    number = 0,
                    row = 3,
                    column = 3,
                    size = 0 // must be set after measurement
                )
            )
        }
    }
    //////////////////////////////
//    private fun createPuzzleData(): List<CellInfo> {
//        return mutableListOf<CellInfo>().apply {
//            for (num in 1..15) {
//                add(
//                    CellInfo(
//                        number = num,
//                        row = (num - 1) / 4,
//                        column = (num - 1) % 4,
//                        size = 0 // must be set after measurement
//                    )
//                )
//            }
//            add(
//                CellInfo(
//                    number = 0,
//                    row = 3,
//                    column = 3,
//                    size = 0 // must be set after measurement
//                )
//            )
//        }
//    }

    fun onCellClicked(clickedCell: CellInfo): Boolean {
        if(!_isGameStarted.value!!) return false
        val size = clickedCell.size
        val emptyCell = _boardData.find { it.number == 0 } ?: return false

        if (clickedCell == emptyCell) return false

        if (clickedCell.actualRow == emptyCell.actualRow) {
            counter++
            val cellsInRow =
                boardData.filter { it.actualRow == emptyCell.actualRow && it != emptyCell }

            val rightToLeft = clickedCell.actualColumn < emptyCell.actualColumn

            if (rightToLeft) {
                val cells = cellsInRow.filter {
                    it.actualColumn >= clickedCell.actualColumn &&
                            it.actualColumn < emptyCell.actualColumn
                }.sortedBy {
                    it.actualColumn
                }
                val xOffsetForEmptyCell = -cells.size * size
                cells.forEach {
                    it.offsetState += IntOffset(x = size, y = 0)
                }
                emptyCell.offsetState += IntOffset(x = xOffsetForEmptyCell, y = 0)
            } else {
                val cells = cellsInRow.filter {
                    it.actualColumn <= clickedCell.actualColumn &&
                            it.actualColumn >= emptyCell.actualColumn
                }.sortedBy {
                    it.actualColumn
                }
                val xOffsetForEmptyCell = cells.size * size
                cells.forEach {
                    it.offsetState += IntOffset(x = -size, y = 0)
                }
                emptyCell.offsetState += IntOffset(x = xOffsetForEmptyCell, y = 0)
            }
        } else if (clickedCell.actualColumn == emptyCell.actualColumn) {
            val cellsInColumn =
                boardData.filter { it.actualColumn == emptyCell.actualColumn && it != emptyCell }

            val topToBottom = clickedCell.actualRow < emptyCell.actualRow

            if (topToBottom) {
                val cells = cellsInColumn.filter {
                    it.actualRow >= clickedCell.actualRow &&
                            it.actualRow < emptyCell.actualRow
                }.sortedBy {
                    it.actualRow
                }
                val yOffsetForEmptyCell = -cells.size * size
                cells.forEach {
                    it.offsetState += IntOffset(x = 0, y = size)
                }
                emptyCell.offsetState += IntOffset(x = 0, y = yOffsetForEmptyCell)
            } else {
                val cells = cellsInColumn.filter {
                    it.actualRow <= clickedCell.actualRow &&
                            it.actualRow > emptyCell.actualRow
                }.sortedBy {
                    it.actualRow
                }
                val yOffsetForEmptyCell = cells.size * size
                cells.forEach {
                    it.offsetState += IntOffset(x = 0, y = -size)
                }
                emptyCell.offsetState += IntOffset(x = 0, y = yOffsetForEmptyCell)
            }
        } else {
            viewModelScope.launch {
                _shakeFlow.emit(clickedCell)
            }
        }

        val result = checkWin(boardData.sortedWith(compareBy { it.number }))
        println("Returning Result $result")
        return result
    }

    private fun checkWin(boardData: List<CellInfo>): Boolean {
        return (boardData[0].number == 0 && boardData[0].actualRow == 3 && boardData[0].actualColumn == 3) &&
                (boardData[1].number == 1 && boardData[1].actualRow == 0 && boardData[1].actualColumn == 0) &&
                (boardData[2].number == 2 && boardData[2].actualRow == 0 && boardData[2].actualColumn == 1) &&
                (boardData[3].number == 3 && boardData[3].actualRow == 0 && boardData[3].actualColumn == 2) &&
                (boardData[4].number == 4 && boardData[4].actualRow == 0 && boardData[4].actualColumn == 3) &&
                (boardData[5].number == 5 && boardData[5].actualRow == 1 && boardData[5].actualColumn == 0) &&
                (boardData[6].number == 6 && boardData[6].actualRow == 1 && boardData[6].actualColumn == 1) &&
                (boardData[7].number == 7 && boardData[7].actualRow == 1 && boardData[7].actualColumn == 2) &&
                (boardData[8].number == 8 && boardData[8].actualRow == 1 && boardData[8].actualColumn == 3) &&
                (boardData[9].number == 9 && boardData[9].actualRow == 2 && boardData[9].actualColumn == 0) &&
                (boardData[10].number == 10 && boardData[10].actualRow == 2 && boardData[10].actualColumn == 1) &&
                (boardData[11].number == 11 && boardData[11].actualRow == 2 && boardData[11].actualColumn == 2) &&
                (boardData[12].number == 12 && boardData[12].actualRow == 2 && boardData[12].actualColumn == 3) &&
                (boardData[13].number == 13 && boardData[13].actualRow == 3 && boardData[13].actualColumn == 0) &&
                (boardData[14].number == 14 && boardData[14].actualRow == 3 && boardData[14].actualColumn == 1) &&
                (boardData[15].number == 15 && boardData[15].actualRow == 3 && boardData[15].actualColumn == 2)
    }

    fun resetGame() {
        _boardData.clear()
        _boardData.addAll(createPuzzleData())
        _isGameReset.value = true
        _isGameStarted.value=false
    }

    fun startGame(){
        _isGameStarted.value=true
    }

    private fun shufflePuzzle(): List<Int> {
        val numRows = 4
        val numColumns = 4
        val numTiles = (1..15).toList()
        val tiles = (1..15).toMutableList()

        // Function to check if a puzzle is solvable
        fun isNotSolvable(tiles: List<Int>): Boolean {
            var inversions = 0
            for (i in 0 until numTiles.size - 1) {
                for (j in i + 1 until numTiles.size) {
                    if (tiles[i] > tiles[j]) {
                        inversions++
                    }
                }
            }
            val blankRow = numRows - tiles.indexOf(0) / numColumns
            return (inversions % 2 == 0 && numRows % 2 == 1) ||
                    (inversions % 2 == 1 && numRows % 2 == 0 && blankRow % 2 == 0)
        }

        // Shuffle until a solvable state is reached
        do {
            tiles.shuffle()
        } while (isNotSolvable(tiles))

        return tiles
    }


    fun saveGridInPref() {
        //Save data in shared pref

    }
}