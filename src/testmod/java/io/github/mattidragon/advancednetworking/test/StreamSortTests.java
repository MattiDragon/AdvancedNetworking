package io.github.mattidragon.advancednetworking.test;

import io.github.mattidragon.advancednetworking.block.ControllerBlock;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class StreamSortTests {
    @GameTest(templateName = "advanced_networking_test:split_items")
    public void splitItems(TestContext context) {
        context.pushButton(4, 3, 2);
        context.addFinalTask(() -> context.expectBlockProperty(new BlockPos(4, 2, 2), ControllerBlock.SUCCESS, true));
    }
}
