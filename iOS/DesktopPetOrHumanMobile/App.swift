import SwiftUI
import UIKit

@main
struct DesktopPetOrHumanMobileApp: App {
    @StateObject private var store = PetStore()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
                .preferredColorScheme(.light)
        }
    }
}

struct Companion: Identifiable {
    let id: String
    let name: String
    let primaryIsHuman: Bool
    let petModeName: String

    static let all: [Companion] = [
        .init(id: "fengge", name: "鋒哥", primaryIsHuman: true, petModeName: "黑貓模式"),
        .init(id: "fengxiong", name: "鋒兄", primaryIsHuman: true, petModeName: "柴犬模式"),
        .init(id: "gugugaga", name: "咕咕嘎嘎", primaryIsHuman: false, petModeName: "企鵝模式"),
        .init(id: "laowangmao", name: "老王貓", primaryIsHuman: false, petModeName: "招財貓模式"),
        .init(id: "miaobaibai", name: "喵白白", primaryIsHuman: false, petModeName: "白貓模式"),
        .init(id: "miaobubu", name: "喵布布", primaryIsHuman: false, petModeName: "三花模式"),
        .init(id: "miaoniang", name: "喵娘", primaryIsHuman: true, petModeName: "貓咪模式"),
        .init(id: "tuge", name: "兔哥", primaryIsHuman: true, petModeName: "兔子模式"),
        .init(id: "xiaotu", name: "小兔", primaryIsHuman: true, petModeName: "兔子模式"),
        .init(id: "xiaoying", name: "小影", primaryIsHuman: true, petModeName: "黑貓模式"),
        .init(id: "yamei", name: "雅美", primaryIsHuman: true, petModeName: "紅貓模式"),
        .init(id: "yumei", name: "玉美", primaryIsHuman: true, petModeName: "海豚模式")
    ]
}

enum PetMood {
    case idle, happy, sleep
}

enum CompanionArt {
    static func image(named name: String) -> UIImage? {
        if let url = Bundle.main.url(forResource: name, withExtension: "png", subdirectory: "Characters") {
            return UIImage(contentsOfFile: url.path)
        }
        if let url = Bundle.main.url(forResource: name, withExtension: "png") {
            return UIImage(contentsOfFile: url.path)
        }
        return UIImage(named: name)
    }

    static func image(id: String, human: Bool, mood: PetMood, primaryIsHuman: Bool) -> UIImage? {
        let moodName: String
        switch mood {
        case .idle: moodName = "idle"
        case .happy: moodName = "happy"
        case .sleep: moodName = "sleep"
        }
        if human, let vector = UIImage(named: "vector_\(id)_human_\(moodName)") {
            return vector
        }
        let preferred: String
        let fallback: String
        if primaryIsHuman {
            if human {
                preferred = "\(id)_\(moodName)"
                fallback = "\(id)_idle"
            } else {
                preferred = "\(id)_pet_\(moodName)"
                fallback = "\(id)_pet_idle"
            }
        } else if human {
            preferred = "\(id)_human_\(moodName)"
            fallback = "\(id)_human_idle"
        } else {
            preferred = "\(id)_\(moodName)"
            fallback = "\(id)_idle"
        }
        return image(named: preferred) ?? image(named: fallback) ?? image(named: "pet_\(moodName)") ?? image(named: "pet_idle")
    }

    static func roster(id: String, primaryIsHuman: Bool) -> UIImage? {
        image(id: id, human: primaryIsHuman, mood: .idle, primaryIsHuman: primaryIsHuman)
    }
}

@MainActor
final class PetStore: ObservableObject {
    static let sizeCount = 5
    static let defaultSizeLevel = 2
    static let sizePoints: [CGFloat] = [132, 168, 210, 260, 324]

    @Published var running = false
    @Published var characterIndex = 0
    @Published var humanForm: [Bool] = Companion.all.map(\.primaryIsHuman)
    @Published var sizeLevel = PetStore.defaultSizeLevel
    @Published var menuVisible = false
    @Published var pickerVisible = false
    @Published var hidden = false
    @Published var bubble = ""
    @Published var bubbleUntil = Date.distantPast
    @Published var cheerUntil = Date.distantPast
    @Published var napUntil = Date.distantPast
    @Published var petOffset = CGSize.zero
    @Published var happiness = 76
    @Published var energy = 66
    @Published var fullness = 63
    @Published var closing = false

    private var idleTimer: Timer?
    private var farewellWork: DispatchWorkItem?
    private var pinchStartLevel = PetStore.defaultSizeLevel

    var companion: Companion { Companion.all[characterIndex] }
    var petSize: CGFloat { PetStore.sizePoints[sizeLevel] }
    var isHuman: Bool { humanForm[characterIndex] }
    var modeLabel: String { isHuman ? "人形模式" : companion.petModeName }

    func start() {
        closing = false
        hidden = false
        running = true
        menuVisible = false
        pickerVisible = false
        say(CharacterTalk.greeting(characterIndex), duration: 3.2)
        restartIdleTimer()
    }

    func stop() {
        idleTimer?.invalidate()
        idleTimer = nil
        farewellWork?.cancel()
        running = false
        closing = false
        menuVisible = false
        pickerVisible = false
        hidden = false
    }

    func greet() {
        guard allowPetAction else { return }
        updateStats(happiness: 3, energy: -1, fullness: -1)
        say(CharacterTalk.idle(characterIndex), duration: 2.4)
    }

    func cheer() {
        guard allowPetAction else { return }
        napUntil = .distantPast
        cheerUntil = Date().addingTimeInterval(3)
        updateStats(happiness: 12, energy: -4, fullness: -2)
        say(CharacterTalk.cheer(characterIndex), duration: 2.4)
    }

    func nap() {
        cheerUntil = .distantPast
        napUntil = Date().addingTimeInterval(3)
        say(CharacterTalk.sleep(characterIndex), duration: 2.4)
        hidePanels()
    }

    func toggleMode() {
        humanForm[characterIndex].toggle()
        say(modeLabel)
        hidePanels()
    }

    func toggleHidden() {
        hidden.toggle()
        say(hidden ? "先躲起來" : "我回來了")
        hidePanels()
    }

    func selectCharacter(_ index: Int) {
        characterIndex = index
        happiness = 82
        energy = max(energy, 48)
        say(CharacterTalk.greeting(characterIndex), duration: 3.2)
        hidePanels()
    }

    func showMenu() {
        guard !closing else { return }
        pickerVisible = false
        menuVisible = true
        say("選單")
    }

    func showPicker() {
        menuVisible = false
        pickerVisible = true
    }

    func hidePanels() {
        menuVisible = false
        pickerVisible = false
    }

    func beginPinch() {
        pinchStartLevel = sizeLevel
        hidePanels()
    }

    func applyPinch(_ scale: CGFloat) {
        let delta = Int((log(max(scale, 0.01)) / log(1.23)).rounded())
        let next = min(PetStore.sizeCount - 1, max(0, pinchStartLevel + delta))
        applySizeLevel(next)
    }

    func dragBy(_ translation: CGSize) {
        hidePanels()
        petOffset.width += translation.width
        petOffset.height += translation.height
        updateStats(happiness: 1, energy: -1, fullness: -1)
    }

    func beginFarewell() {
        guard !closing else { return }
        closing = true
        hidePanels()
        say(CharacterTalk.farewell(characterIndex), duration: 2.4)
        let work = DispatchWorkItem { [weak self] in
            self?.stop()
        }
        farewellWork = work
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.8, execute: work)
    }

    func currentImage(at now: Date) -> UIImage? {
        let mood: PetMood
        if now < cheerUntil {
            mood = .happy
        } else if now < napUntil || energy < 34 {
            mood = .sleep
        } else if happiness > 78 {
            mood = .happy
        } else {
            mood = .idle
        }
        return CompanionArt.image(
            id: companion.id,
            human: isHuman,
            mood: mood,
            primaryIsHuman: companion.primaryIsHuman
        )
    }

    func cheerHop(at now: Date) -> CGFloat {
        guard now < cheerUntil else { return 0 }
        let elapsed = 3 - cheerUntil.timeIntervalSince(now)
        if elapsed >= 1.2 { return 0 }
        return abs(sin(elapsed * .pi * 3.5)) * 16 * max(0, 1 - elapsed / 1.2)
    }

    private var allowPetAction: Bool {
        !closing && !menuVisible && !pickerVisible && !hidden
    }

    private func applySizeLevel(_ level: Int) {
        guard level != sizeLevel else { return }
        sizeLevel = level
        say("大小 \(sizeLevel + 1)/\(PetStore.sizeCount)")
    }

    private func maybeIdleTalk() {
        guard allowPetAction, Date() >= bubbleUntil else { return }
        say(CharacterTalk.idle(characterIndex), duration: 2.4)
    }

    private func say(_ text: String, duration: TimeInterval = 1.8) {
        bubble = text
        bubbleUntil = Date().addingTimeInterval(duration)
    }

    private func updateStats(happiness h: Int, energy e: Int, fullness f: Int) {
        happiness = min(100, max(0, happiness + h))
        energy = min(100, max(0, energy + e))
        fullness = min(100, max(0, fullness + f))
    }

    private func restartIdleTimer() {
        idleTimer?.invalidate()
        idleTimer = Timer.scheduledTimer(withTimeInterval: 8.5, repeats: true) { [weak self] _ in
            Task { @MainActor in
                self?.maybeIdleTalk()
            }
        }
    }
}

struct RootView: View {
    @EnvironmentObject private var store: PetStore

    var body: some View {
        if store.running {
            DesktopView()
        } else {
            LauncherView()
        }
    }
}

struct LauncherView: View {
    @EnvironmentObject private var store: PetStore

    var body: some View {
        ZStack {
            Color(red: 248 / 255, green: 246 / 255, blue: 241 / 255).ignoresSafeArea()
            VStack(spacing: 18) {
                if let icon = CompanionArt.roster(id: "fengge", primaryIsHuman: true) {
                    Image(uiImage: icon)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 148, height: 148)
                }
                Text("讓桌寵出現在桌面上")
                    .font(.system(size: 26, weight: .bold))
                    .foregroundStyle(Color(red: 30 / 255, green: 31 / 255, blue: 36 / 255))
                    .multilineTextAlignment(.center)
                Text("iOS 無法讓第三方 App 浮在其他 App 上層。啟動後，桌寵會待在本 App 的桌面裡：可拖曳、長按開選單、點一下說話、連點開心、雙指縮放大小，並切換 12 位角色與人形／寵物模式。")
                    .font(.system(size: 16))
                    .foregroundStyle(Color(red: 75 / 255, green: 79 / 255, blue: 87 / 255))
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
                    .padding(.bottom, 8)
                Button("啟動桌寵") { store.start() }
                    .buttonStyle(PetButtonStyle())
            }
            .padding(.horizontal, 28)
        }
    }
}

struct PetButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .semibold))
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity, minHeight: 52)
            .background(Color(red: 30 / 255, green: 31 / 255, blue: 36 / 255), in: RoundedRectangle(cornerRadius: 14))
            .opacity(configuration.isPressed ? 0.82 : 1)
    }
}

struct DesktopView: View {
    @EnvironmentObject private var store: PetStore

    var body: some View {
        TimelineView(.animation(minimumInterval: 1.0 / 30.0, paused: false)) { timeline in
            GeometryReader { geo in
                let now = timeline.date
                ZStack {
                    HomeWallpaper()
                    if store.menuVisible || store.pickerVisible {
                        Color.black.opacity(0.01)
                            .ignoresSafeArea()
                            .onTapGesture { store.hidePanels() }
                    }
                    if store.hidden {
                        Button("顯示桌寵") { store.toggleHidden() }
                            .font(.system(size: 14, weight: .semibold))
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                            .background(.white.opacity(0.92), in: Capsule())
                            .position(x: geo.size.width - 84, y: 58)
                    } else {
                        petLayer(in: geo.size, now: now)
                    }
                    if store.menuVisible {
                        MenuPanel()
                            .frame(width: 204)
                            .position(x: geo.size.width - 118, y: 168)
                    }
                    if store.pickerVisible {
                        PickerPanel()
                            .padding(12)
                            .frame(maxWidth: 420)
                            .position(x: geo.size.width / 2, y: min(340, geo.size.height * 0.42))
                    }
                }
            }
        }
        .ignoresSafeArea()
    }

    @ViewBuilder
    private func petLayer(in size: CGSize, now: Date) -> some View {
        let pet = store.petSize
        let hop = store.cheerHop(at: now)
        let defaultX = size.width - pet / 2 - 22
        let defaultY = size.height - pet / 2 - 96
        let x = min(size.width - pet / 2 - 8, max(pet / 2 + 8, defaultX + store.petOffset.width))
        let y = min(size.height - pet / 2 - 8, max(pet / 2 + 88, defaultY + store.petOffset.height - hop))
        let showBubble = now < store.bubbleUntil && !store.bubble.isEmpty && !store.pickerVisible

        ZStack {
            if showBubble {
                SpeechBubble(text: store.bubble)
                    .offset(y: -(pet * 0.42 + 28))
            }
            PetSprite(image: store.currentImage(at: now), size: pet, now: now)
            PetGestureSurface(
                onTap: { store.greet() },
                onDoubleTap: { store.cheer() },
                onLongPress: { store.showMenu() },
                onDrag: { store.dragBy($0) },
                onPinchBegan: { store.beginPinch() },
                onPinch: { store.applyPinch($0) }
            )
            .frame(width: pet, height: pet)
        }
        .frame(width: pet, height: pet)
        .position(x: x, y: y)
    }
}

struct HomeWallpaper: View {
    private let apps: [(String, String, Color)] = [
        ("電話", "phone.fill", .green),
        ("訊息", "message.fill", .green),
        ("Safari", "safari.fill", .blue),
        ("郵件", "envelope.fill", .blue),
        ("音樂", "music.note", .pink),
        ("照片", "photo.fill", .orange),
        ("地圖", "map.fill", .green),
        ("設定", "gearshape.fill", .gray)
    ]

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.45, green: 0.62, blue: 0.92),
                    Color(red: 0.62, green: 0.48, blue: 0.86),
                    Color(red: 0.95, green: 0.72, blue: 0.55)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            VStack(spacing: 18) {
                VStack(spacing: 4) {
                    Text(Date.now, format: .dateTime.weekday(.wide).month().day())
                        .font(.system(size: 18, weight: .medium))
                    Text(Date.now, format: .dateTime.hour().minute())
                        .font(.system(size: 64, weight: .semibold, design: .rounded))
                }
                .foregroundStyle(.white)
                .padding(.top, 72)

                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 22), count: 4), spacing: 22) {
                    ForEach(apps, id: \.0) { item in
                        VStack(spacing: 6) {
                            RoundedRectangle(cornerRadius: 16)
                                .fill(item.2.gradient)
                                .frame(width: 58, height: 58)
                                .overlay {
                                    Image(systemName: item.1)
                                        .font(.system(size: 24, weight: .semibold))
                                        .foregroundStyle(.white)
                                }
                            Text(item.0)
                                .font(.caption)
                                .foregroundStyle(.white)
                        }
                    }
                }
                .padding(.horizontal, 28)
                .padding(.top, 12)

                Spacer()

                Text("長按桌寵開選單 · 點一下說話 · 連點開心")
                    .font(.caption)
                    .foregroundStyle(.white.opacity(0.9))
                    .padding(.bottom, 28)
            }
        }
    }
}

struct PetSprite: View {
    let image: UIImage?
    let size: CGFloat
    let now: Date

    var body: some View {
        let breathe = 1 + sin(now.timeIntervalSinceReferenceDate * 2.2) * 0.018
        let tilt = sin(now.timeIntervalSinceReferenceDate * 1.3) * 2.3
        ZStack {
            Ellipse()
                .fill(Color.black.opacity(0.22))
                .frame(width: size * 0.64, height: size * 0.12)
                .offset(y: size * 0.38)
            if let image {
                Image(uiImage: image)
                    .resizable()
                    .interpolation(.high)
                    .scaledToFit()
                    .frame(width: size, height: size)
            } else {
                Circle()
                    .fill(Color(red: 0.11, green: 0.12, blue: 0.14))
                    .frame(width: size * 0.7, height: size * 0.7)
            }
        }
        .scaleEffect(x: breathe, y: 1 / breathe)
        .rotationEffect(.degrees(tilt))
    }
}

struct SpeechBubble: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.system(size: 14))
            .foregroundStyle(Color(red: 28 / 255, green: 31 / 255, blue: 36 / 255))
            .multilineTextAlignment(.center)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .frame(minWidth: 74, maxWidth: 188)
            .background(.white.opacity(0.93), in: RoundedRectangle(cornerRadius: 17))
    }
}

struct MenuPanel: View {
    @EnvironmentObject private var store: PetStore

    private var items: [(String, Color, () -> Void)] {
        [
            ("角色", Color(red: 81 / 255, green: 156 / 255, blue: 1), { store.showPicker() }),
            ("模式", Color(red: 1, green: 193 / 255, blue: 87 / 255), { store.toggleMode() }),
            ("睡一下", Color(red: 164 / 255, green: 148 / 255, blue: 1), { store.nap() }),
            (store.hidden ? "顯示桌寵" : "先躲起來", Color(red: 80 / 255, green: 196 / 255, blue: 186 / 255), { store.toggleHidden() }),
            ("關閉", Color(red: 1, green: 100 / 255, blue: 100 / 255), { store.beginFarewell() })
        ]
    }

    var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                if index > 0 {
                    Rectangle()
                        .fill(Color.white.opacity(0.18))
                        .frame(height: 1)
                        .padding(.leading, 52)
                        .padding(.trailing, 14)
                }
                Button(action: item.2) {
                    HStack(spacing: 12) {
                        ZStack {
                            Circle().fill(item.1).frame(width: 28, height: 28)
                            Text(item.0 == "關閉" ? "X" : String(item.0.prefix(1)))
                                .font(.system(size: 13, weight: .bold))
                                .foregroundStyle(.white)
                        }
                        Text(item.0)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundStyle(.white)
                        Spacer()
                    }
                    .padding(.horizontal, 14)
                    .frame(height: 46)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.vertical, 7)
        .background(
            LinearGradient(
                colors: [
                    Color(red: 45 / 255, green: 46 / 255, blue: 52 / 255).opacity(0.94),
                    Color(red: 20 / 255, green: 21 / 255, blue: 25 / 255).opacity(0.94)
                ],
                startPoint: .top,
                endPoint: .bottom
            ),
            in: RoundedRectangle(cornerRadius: 22)
        )
    }
}

struct PickerPanel: View {
    @EnvironmentObject private var store: PetStore

    var body: some View {
        VStack(spacing: 8) {
            Text("選擇角色")
                .font(.system(size: 15, weight: .bold))
                .foregroundStyle(.white)
            Text("點選頭像切換  ·  點標題返回")
                .font(.system(size: 11))
                .foregroundStyle(Color.white.opacity(0.7))
                .onTapGesture { store.showMenu() }
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 6), count: 3), spacing: 6) {
                ForEach(Array(Companion.all.enumerated()), id: \.offset) { index, companion in
                    Button {
                        store.selectCharacter(index)
                    } label: {
                        VStack(spacing: 4) {
                            if let image = CompanionArt.roster(id: companion.id, primaryIsHuman: companion.primaryIsHuman) {
                                Image(uiImage: image)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 72)
                            }
                            Text(companion.name)
                                .font(.system(size: 11, weight: index == store.characterIndex ? .bold : .regular))
                                .foregroundStyle(index == store.characterIndex ? Color(red: 168 / 255, green: 206 / 255, blue: 1) : .white)
                        }
                        .padding(6)
                        .frame(maxWidth: .infinity)
                        .background(
                            RoundedRectangle(cornerRadius: 16)
                                .fill(index == store.characterIndex ? Color(red: 81 / 255, green: 156 / 255, blue: 1).opacity(0.35) : .clear)
                        )
                        .overlay {
                            if index == store.characterIndex {
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color(red: 120 / 255, green: 180 / 255, blue: 1), lineWidth: 2)
                            }
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(12)
        .background(
            LinearGradient(
                colors: [
                    Color(red: 45 / 255, green: 46 / 255, blue: 52 / 255).opacity(0.96),
                    Color(red: 20 / 255, green: 21 / 255, blue: 25 / 255).opacity(0.96)
                ],
                startPoint: .top,
                endPoint: .bottom
            ),
            in: RoundedRectangle(cornerRadius: 22)
        )
    }
}

struct PetGestureSurface: UIViewRepresentable {
    var onTap: () -> Void
    var onDoubleTap: () -> Void
    var onLongPress: () -> Void
    var onDrag: (CGSize) -> Void
    var onPinchBegan: () -> Void
    var onPinch: (CGFloat) -> Void

    func makeUIView(context: Context) -> GestureView {
        let view = GestureView()
        view.coordinator = context.coordinator
        return view
    }

    func updateUIView(_ uiView: GestureView, context: Context) {
        context.coordinator.onTap = onTap
        context.coordinator.onDoubleTap = onDoubleTap
        context.coordinator.onLongPress = onLongPress
        context.coordinator.onDrag = onDrag
        context.coordinator.onPinchBegan = onPinchBegan
        context.coordinator.onPinch = onPinch
        uiView.coordinator = context.coordinator
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    final class Coordinator {
        var onTap: () -> Void = {}
        var onDoubleTap: () -> Void = {}
        var onLongPress: () -> Void = {}
        var onDrag: (CGSize) -> Void = { _ in }
        var onPinchBegan: () -> Void = {}
        var onPinch: (CGFloat) -> Void = { _ in }
        var lastPan: CGPoint = .zero
        var pinching = false
    }

    final class GestureView: UIView, UIGestureRecognizerDelegate {
        weak var coordinator: Coordinator?
        private var didAddRecognizers = false

        override init(frame: CGRect) {
            super.init(frame: frame)
            backgroundColor = .clear
            isMultipleTouchEnabled = true
        }

        required init?(coder: NSCoder) { nil }

        override func didMoveToWindow() {
            super.didMoveToWindow()
            guard window != nil, !didAddRecognizers else { return }
            didAddRecognizers = true
            let doubleTap = UITapGestureRecognizer(target: self, action: #selector(handleDoubleTap))
            doubleTap.numberOfTapsRequired = 2
            let tap = UITapGestureRecognizer(target: self, action: #selector(handleTap))
            tap.require(toFail: doubleTap)
            let longPress = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress))
            longPress.minimumPressDuration = 0.5
            let pan = UIPanGestureRecognizer(target: self, action: #selector(handlePan))
            pan.maximumNumberOfTouches = 1
            let pinch = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch))
            [doubleTap, tap, longPress, pan, pinch].forEach { recognizer in
                recognizer.delegate = self
                addGestureRecognizer(recognizer)
            }
        }

        @objc private func handleTap() { coordinator?.onTap() }
        @objc private func handleDoubleTap() { coordinator?.onDoubleTap() }
        @objc private func handleLongPress(_ gesture: UILongPressGestureRecognizer) {
            if gesture.state == .began { coordinator?.onLongPress() }
        }
        @objc private func handlePan(_ gesture: UIPanGestureRecognizer) {
            guard coordinator?.pinching != true else { return }
            let point = gesture.translation(in: self)
            if gesture.state == .began {
                coordinator?.lastPan = .zero
            }
            let last = coordinator?.lastPan ?? .zero
            coordinator?.onDrag(CGSize(width: point.x - last.x, height: point.y - last.y))
            coordinator?.lastPan = point
        }
        @objc private func handlePinch(_ gesture: UIPinchGestureRecognizer) {
            if gesture.state == .began {
                coordinator?.pinching = true
                coordinator?.onPinchBegan()
            }
            if gesture.state == .changed || gesture.state == .ended {
                coordinator?.onPinch(gesture.scale)
            }
            if gesture.state == .ended || gesture.state == .cancelled {
                coordinator?.pinching = false
            }
        }

        func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
            false
        }
    }
}
