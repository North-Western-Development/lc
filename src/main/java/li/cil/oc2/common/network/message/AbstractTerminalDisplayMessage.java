/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.network.message;

import li.cil.oc2.common.blockentity.ComputerBlockEntity;
import li.cil.oc2.common.blockentity.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.ByteBuffer;

public abstract class AbstractTerminalDisplayMessage extends AbstractMessage {
    protected BlockPos pos;
    protected byte[] data;

    ///////////////////////////////////////////////////////////////////

    protected AbstractTerminalDisplayMessage(final MonitorBlockEntity monitor, final ByteBuffer data) {
        this.pos = monitor.getBlockPos();
        this.data = data.array();
    }

    protected AbstractTerminalDisplayMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        data = buffer.readByteArray();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeByteArray(data);
    }
}
