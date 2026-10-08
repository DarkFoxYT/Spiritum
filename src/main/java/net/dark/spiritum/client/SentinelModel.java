package net.dark.spiritum.client;

import com.google.gson.*;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class SentinelModel extends EntityModel<SentinelRenderer.State> {
    private static final JsonObject DATA = load();
    private final Map<String, Clip> clips = new HashMap<>();

    public SentinelModel() {
        super(createModel());
        for (var entry : DATA.getAsJsonObject("animations").entrySet()) {
            JsonObject animation = entry.getValue().getAsJsonObject();
            List<Track> tracks = new ArrayList<>();
            for (var value : animation.getAsJsonArray("tracks")) {
                JsonObject track = value.getAsJsonObject();
                ModelPart part = root;
                for (var name : track.getAsJsonArray("path"))
                    part = part.getChild(name.getAsString());
                List<Frame> frames = new ArrayList<>();
                for (var frameValue : track.getAsJsonArray("frames")) {
                    JsonObject frame = frameValue.getAsJsonObject();
                    frames.add(
                            new Frame(
                                    frame.get("time").getAsFloat(),
                                    vector(frame, "value"),
                                    frame.get("smooth").getAsBoolean()));
                }
                tracks.add(
                        new Track(
                                part,
                                track.get("channel").getAsString().equals("rotation"),
                                frames));
            }
            clips.put(entry.getKey(), new Clip(animation.get("length").getAsFloat(), tracks));
        }
    }

    private static JsonObject load() {
        var stream =
                SentinelModel.class.getResourceAsStream(
                        "/assets/spiritum/models/entity/sentinel.json");
        if (stream == null) throw new IllegalStateException("Missing Sentinel model");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot load Sentinel model", e);
        }
    }

    private static ModelPart createModel() {
        ModelData model = new ModelData();
        for (var bone : DATA.getAsJsonArray("bones"))
            addBone(model.getRoot(), bone.getAsJsonObject());
        return TexturedModelData.of(
                        model, DATA.get("width").getAsInt(), DATA.get("height").getAsInt())
                .createModel();
    }

    private static void addBone(ModelPartData parent, JsonObject bone) {
        var part =
                parent.addChild(
                        bone.get("name").getAsString(), ModelPartBuilder.create(), transform(bone));
        int index = 0;
        for (var value : bone.getAsJsonArray("cubes")) {
            JsonObject cube = value.getAsJsonObject();
            float[] from = vector(cube, "from"),
                    size = vector(cube, "size"),
                    uv = vector(cube, "uv");
            var builder =
                    ModelPartBuilder.create()
                            .uv((int) uv[0], (int) uv[1])
                            .mirrored(cube.get("mirror").getAsBoolean())
                            .cuboid(
                                    from[0],
                                    from[1],
                                    from[2],
                                    size[0],
                                    size[1],
                                    size[2],
                                    new Dilation(cube.get("inflate").getAsFloat()));
            part.addChild("cube_" + index++, builder, transform(cube));
        }
        for (var child : bone.getAsJsonArray("children")) addBone(part, child.getAsJsonObject());
    }

    private static ModelTransform transform(JsonObject object) {
        float[] origin = vector(object, "origin"), rotation = vector(object, "rotation");
        return ModelTransform.of(
                origin[0],
                origin[1],
                origin[2],
                radians(rotation[0]),
                radians(rotation[1]),
                radians(rotation[2]));
    }

    private static float[] vector(JsonObject object, String key) {
        JsonArray values = object.getAsJsonArray(key);
        float[] result = new float[values.size()];
        for (int i = 0; i < result.length; i++) result[i] = values.get(i).getAsFloat();
        return result;
    }

    private static float radians(float degrees) {
        return degrees * (float) Math.PI / 180;
    }

    @Override
    public void setAngles(SentinelRenderer.State state) {
        super.setAngles(state);
        if (!state.awake) {
            play("guard_pose", 0, 1);
            return;
        }
        play("aggro_pose", 0, 1);
        if (state.attack != 0) {
            String name = state.attack == 1 ? "attack1" : "attack2";
            float duration = state.attack == 1 ? 12 : 10;
            play(name, (1 - state.attackRemaining / duration) * clips.get(name).length(), 1);
        } else if (state.awakeTime < 20) {
            play("aggro", state.awakeTime / 20, 1);
        } else {
            play(
                    "walk",
                    (state.limbSwingAnimationProgress / ((float) Math.PI * 2)) % 1,
                    Math.min(1, state.limbSwingAmplitude));
        }
        ModelPart head = root.getChild("main").getChild("body").getChild("head");
        head.pitch += radians(state.pitch);
        head.yaw += radians(state.relativeHeadYaw);
    }

    private void play(String name, float time, float weight) {
        for (Track track : clips.get(name).tracks()) {
            float[] value = track.sample(time);
            ModelPart part = track.part();
            if (track.rotation()) {
                part.pitch = MathHelper.lerp(weight, part.pitch, radians(value[0]));
                part.yaw = MathHelper.lerp(weight, part.yaw, radians(value[1]));
                part.roll = MathHelper.lerp(weight, part.roll, radians(value[2]));
            } else {
                ModelTransform origin = part.getDefaultTransform();
                part.originX = MathHelper.lerp(weight, part.originX, origin.x() + value[0]);
                part.originY = MathHelper.lerp(weight, part.originY, origin.y() + value[1]);
                part.originZ = MathHelper.lerp(weight, part.originZ, origin.z() + value[2]);
            }
        }
    }

    private record Clip(float length, List<Track> tracks) {}

    private record Frame(float time, float[] value, boolean smooth) {}

    private record Track(ModelPart part, boolean rotation, List<Frame> frames) {
        float[] sample(float time) {
            if (time <= frames.getFirst().time()) return frames.getFirst().value();
            if (time >= frames.getLast().time()) return frames.getLast().value();
            int i = 0;
            while (i + 1 < frames.size() && frames.get(i + 1).time() < time) i++;
            Frame first = frames.get(i), second = frames.get(i + 1);
            float t = (time - first.time()) / (second.time() - first.time());
            float[] value = new float[3];
            for (int axis = 0; axis < 3; axis++) {
                if (second.smooth()) {
                    float a = frames.get(Math.max(0, i - 1)).value()[axis], b = first.value()[axis];
                    float c = second.value()[axis],
                            d = frames.get(Math.min(frames.size() - 1, i + 2)).value()[axis];
                    value[axis] =
                            .5f
                                    * ((2 * b)
                                            + (-a + c) * t
                                            + (2 * a - 5 * b + 4 * c - d) * t * t
                                            + (-a + 3 * b - 3 * c + d) * t * t * t);
                } else value[axis] = MathHelper.lerp(t, first.value()[axis], second.value()[axis]);
            }
            return value;
        }
    }
}
