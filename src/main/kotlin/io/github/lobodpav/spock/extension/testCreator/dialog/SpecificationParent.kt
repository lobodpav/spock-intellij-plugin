package io.github.lobodpav.spock.extension.testCreator.dialog

/** A fully qualified class name that can be a parent of newly-created Specifications */
@JvmInline
value class SpecificationParent(val fqClassName: String) {
    /** Makes sure the item in the [io.github.lobodpav.spock.extension.testCreator.dialog.CreateSpecificationDialog] is shown as a [String] */
    override fun toString(): String = fqClassName
}
