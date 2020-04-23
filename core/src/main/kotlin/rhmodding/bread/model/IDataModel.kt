package rhmodding.bread.model


interface IDataModel {
    
    val sprites: MutableList<out ISprite>
    val animations: MutableList<out IAnimation>
    val sheetW: UShort
    val sheetH: UShort
    
}
