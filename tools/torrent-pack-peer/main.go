// Loopback-only transport bridge for a generated season-pack CI fixture.
package main

import (
	"bytes"
	"context"
	"encoding/hex"
	"encoding/json"
	"flag"
	"fmt"
	"github.com/anacrolix/torrent/mse"
	"io"
	"log"
	"net"
	"os"
	"time"
)

func main() {
	fixturePath := flag.String("fixture", "", "JSON from the generated loopback seed")
	flag.Parse()
	data, err := os.ReadFile(*fixturePath)
	must(err)
	fixture := map[string]any{}
	must(json.Unmarshal(data, &fixture))
	runLoopbackSeed(fixture)
}

func runLoopbackSeed(fixture map[string]any) {
	seedPort, seedOK := fixture["seed_port"].(float64)
	proxyPort, proxyOK := fixture["seed_proxy_port"].(float64)
	infoHash, err := hex.DecodeString(fmt.Sprint(fixture["info_hash"]))
	if err != nil || len(infoHash) != 20 || !seedOK || !proxyOK || seedPort < 1 || seedPort > 65535 || proxyPort < 1 || proxyPort > 65535 {
		log.Fatal("Invalid loopback-only fixture")
	}
	listener, err := net.Listen("tcp", fmt.Sprintf("127.0.0.1:%d", int(proxyPort)))
	must(err)
	defer listener.Close()
	fmt.Println("Loopback-only fixture bridge ready")
	for {
		incoming, err := listener.Accept()
		must(err)
		go func() {
			defer incoming.Close()
			stream, err := receiveTorrentStream(incoming, infoHash)
			if err != nil {
				return
			}
			outgoing, err := net.DialTimeout("tcp", fmt.Sprintf("127.0.0.1:%d", int(seedPort)), 5*time.Second)
			if err != nil {
				return
			}
			defer outgoing.Close()
			done := make(chan struct{})
			go func() { io.Copy(outgoing, stream); outgoing.Close(); close(done) }()
			io.Copy(stream, outgoing)
			incoming.Close()
			<-done
		}()
	}
}

type bufferedStream struct {
	io.Reader
	io.Writer
}

// The Android engine accepts a plain BEP handshake; desktop TorrServer prefers
// MSE. Decode its transport using the upstream implementation before forwarding
// to the same bounded loopback fixture. No VPN or host network configuration is used.
func receiveTorrentStream(conn net.Conn, infoHash []byte) (io.ReadWriter, error) {
	conn.SetDeadline(time.Now().Add(15 * time.Second))
	defer conn.SetDeadline(time.Time{})
	header := make([]byte, 20)
	if _, err := io.ReadFull(conn, header); err != nil {
		return nil, err
	}
	stream := bufferedStream{io.MultiReader(bytes.NewReader(header), conn), conn}
	if bytes.Equal(header, []byte("\x13BitTorrent protocol")) {
		return stream, nil
	}
	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()
	plain, _, err := mse.ReceiveHandshake(ctx, stream, func(yield func([]byte) bool) { yield(infoHash) }, mse.DefaultCryptoSelector)
	return plain, err
}

func must(err error) {
	if err != nil {
		log.Fatal(err)
	}
}
